package isi.dan.ms.pedidos.servicio;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import isi.dan.ms.pedidos.conf.RabbitMQConfig;
import isi.dan.ms.pedidos.dao.PedidoRepository;
import isi.dan.ms.pedidos.dto.ClienteDTO;
import isi.dan.ms.pedidos.dto.CrearPedidoDTO;
import isi.dan.ms.pedidos.modelo.Cliente;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.modelo.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClienteService clienteService;

    Logger log = LoggerFactory.getLogger(PedidoService.class);

    @Transactional
    public Pedido savePedido(CrearPedidoDTO crearPedidoDTO) {
        log.info("Iniciando creación de pedido para cliente: {}", crearPedidoDTO.getClienteId());

        // Crear nuevo pedido
        Pedido pedido = new Pedido();

        // Paso 1: Asignar número de pedido y fecha
        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setFechaPedido(LocalDateTime.now());
        log.info("Pedido creado con número: {}", pedido.getNumeroPedido());

        // Verificar que el cliente existe
        ClienteDTO clienteDTO = clienteService.obtenerCliente(crearPedidoDTO.getClienteId());
        if (clienteDTO == null) {
            log.error("Cliente no encontrado con ID: {}", crearPedidoDTO.getClienteId());
            pedido.setEstado(Pedido.EstadoPedido.RECHAZADO);
            return pedidoRepository.save(pedido);
        }

        // Asignar datos básicos del pedido
        pedido.setClienteId(clienteDTO.getId());
        pedido.setObraId(crearPedidoDTO.getObraId());
        pedido.setObservaciones(crearPedidoDTO.getObservaciones());

        // Crear detalles del pedido y calcular montos
        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal montoTotal = BigDecimal.ZERO;

        for (CrearPedidoDTO.DetallePedidoDTO detalleDTO : crearPedidoDTO.getDetalles()) {
            // Buscar producto (mock por ahora)
            // Producto producto = productoRepository.findById(detalleDTO.getProductoId())
            // .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Producto producto = new Producto();
            producto.setId(detalleDTO.getProductoId());
            // Aquí deberías obtener el precio del producto desde su entidad
            BigDecimal precioUnitario = BigDecimal.valueOf(100.0); // Mock price

            DetallePedido detalle = new DetallePedido(producto, detalleDTO.getCantidad(), precioUnitario);
            detalle.setPedido(pedido);
            detalle.calcularMontoLinea();

            detalles.add(detalle);
            montoTotal = montoTotal.add(detalle.getMontoLinea());
        }

        pedido.setDetalles(detalles);
        pedido.setMontoTotal(montoTotal);

        log.info("Monto total calculado: {}", montoTotal);

        // Paso 2: Verificar saldo del cliente
        boolean tieneSaldoSuficiente = clienteService.verificarSaldoCliente(
                crearPedidoDTO.getClienteId(),
                montoTotal);

        if (!tieneSaldoSuficiente) {
            log.warn("Cliente {} no tiene saldo suficiente para el pedido. Monto requerido: {}",
                    crearPedidoDTO.getClienteId(), montoTotal);
            pedido.setEstado(Pedido.EstadoPedido.RECHAZADO);
            Pedido pedidoRechazado = pedidoRepository.save(pedido);
            log.info("Pedido {} RECHAZADO por falta de saldo", pedido.getNumeroPedido());
            return pedidoRechazado;
        }

        log.info("Cliente tiene saldo suficiente. Continuando con el procesamiento...");

        // Si tiene saldo, continuar con la verificación de stock (Paso 3)
        // TODO: Implementar verificación y actualización de stock

        // Por ahora, marcar como pendiente hasta implementar el paso 3
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido {} guardado exitosamente con estado: {}",
                pedidoGuardado.getNumeroPedido(), pedidoGuardado.getEstado());

        return pedidoGuardado;
    }

    private String generarNumeroPedido() {
        // Generar número de pedido único basado en fecha y hora
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PED-" + timestamp;
    }

    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido getPedidoById(String id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public void deletePedido(String id) {
        pedidoRepository.deleteById(id);
    }
}
