package isi.dan.ms.pedidos.servicio;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import isi.dan.ms.pedidos.conf.RabbitMQConfig;
import isi.dan.ms.pedidos.dao.PedidoRepository;
import isi.dan.ms.pedidos.dto.ClienteDTO;
import isi.dan.ms.pedidos.dto.CrearPedidoDTO;
import isi.dan.ms.pedidos.dto.ItemOrdenDTO;
import isi.dan.ms.pedidos.dto.OrdenEjecutadaDTO;
import isi.dan.ms.pedidos.dto.ProductoDTO;
import isi.dan.ms.pedidos.dto.StockDevolucionDTO;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private ProductoService productoService;

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
        ClienteDTO clienteDTO;
        try {
            clienteDTO = clienteService.obtenerCliente(crearPedidoDTO.getClienteId());
            if (clienteDTO == null) {
                log.error("Cliente no encontrado con ID: {}", crearPedidoDTO.getClienteId());
                pedido.setEstado(Pedido.EstadoPedido.RECHAZADO);
                return pedidoRepository.save(pedido);
            }
        } catch (Exception e) {
            log.error("Error al consultar cliente: {}", e.getMessage());
            pedido.setEstado(Pedido.EstadoPedido.RECIBIDO);
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
            // Obtener información real del producto
            ProductoDTO productoDTO;
            try {
                productoDTO = productoService.obtenerProducto(detalleDTO.getProductoId());
                if (productoDTO == null) {
                    log.error("Producto no encontrado con ID: {}", detalleDTO.getProductoId());
                    pedido.setEstado(Pedido.EstadoPedido.RECHAZADO);
                    return pedidoRepository.save(pedido);
                }
            } catch (Exception e) {
                log.error("Error al consultar producto: {}", e.getMessage());
                pedido.setEstado(Pedido.EstadoPedido.RECIBIDO);
                return pedidoRepository.save(pedido);
            }

            // Crear detalle con ID del producto (no objeto completo)
            DetallePedido detalle = new DetallePedido(
                    productoDTO.getId(),
                    detalleDTO.getCantidad(),
                    productoDTO.getPrecio());
            detalle.calcularMontoLinea();

            detalles.add(detalle);
            montoTotal = montoTotal.add(detalle.getMontoLinea());
        }

        pedido.setDetalles(detalles);
        pedido.setMontoTotal(montoTotal);

        log.info("Monto total calculado: {}", montoTotal);

        // Paso 2: Verificar saldo del cliente
        try {
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
            } else {
                log.info("Cliente tiene saldo suficiente. Pedido {} pasa a ACEPTADO", pedido.getNumeroPedido());
                pedido.setEstado(Pedido.EstadoPedido.ACEPTADO);
            }

        } catch (Exception e) {
            log.error("Error al verificar saldo del cliente: {}", e.getMessage());
            pedido.setEstado(Pedido.EstadoPedido.RECIBIDO);
            Pedido pedidoRecibido = pedidoRepository.save(pedido);
            log.info("Pedido {} queda en estado RECIBIDO por error en verificación de saldo", pedido.getNumeroPedido());
            return pedidoRecibido;
        }

        log.info("Cliente tiene saldo suficiente. Continuando con verificación de stock...");

        // Paso 3: Verificar stock y enviar orden para actualización
        boolean todosSuficienteStock = true;

        try {
            // Primero verificamos que todos los productos tengan stock suficiente
            for (DetallePedido detalle : detalles) {
                boolean tieneStock = productoService.verificarStockDisponible(
                        detalle.getProductoId(),
                        detalle.getCantidad());

                if (!tieneStock) {
                    log.warn("Producto {} no tiene stock suficiente para cantidad: {}",
                            detalle.getProductoId(), detalle.getCantidad());
                    todosSuficienteStock = false;
                    break;
                }
            }

            if (!todosSuficienteStock) {
                log.info("No hay stock suficiente para algunos productos. Pedido {} queda ACEPTADO",
                        pedido.getNumeroPedido());
                // Ya está en ACEPTADO, no cambiar estado
            } else {
                // Enviar orden de actualización de stock a través de RabbitMQ
                boolean stockActualizado = enviarOrdenActualizacionStock(pedido);

                if (stockActualizado) {
                    log.info("Orden de actualización de stock enviada correctamente. Pedido {} pasa a EN_PREPARACION",
                            pedido.getNumeroPedido());
                    pedido.setEstado(Pedido.EstadoPedido.EN_PREPARACION);
                } else {
                    log.warn("No se pudo enviar la orden de actualización de stock. Pedido {} queda ACEPTADO",
                            pedido.getNumeroPedido());
                    // Ya está en ACEPTADO, no cambiar estado
                }
            }
        } catch (Exception e) {
            log.error("Error en verificación de stock: {}", e.getMessage());
            // Mantener en ACEPTADO si hay errores en la verificación de stock
        }

        Pedido pedidoGuardado;

        try {
            pedidoGuardado = pedidoRepository.save(pedido);
            log.info("Pedido {} guardado exitosamente con estado: {}",
                    pedidoGuardado.getNumeroPedido(), pedidoGuardado.getEstado());
        } catch (Exception e) {
            log.error("Error al guardar el pedido {}: {}",
                    pedido.getNumeroPedido(), e.getMessage());
            throw e; // Re-lanzar la excepción para que la transacción se revierta
        }

        return pedidoGuardado;
    }

    private boolean enviarOrdenActualizacionStock(Pedido pedido) {
        try {
            // Convertir detalles del pedido a ItemOrdenDTO
            List<ItemOrdenDTO> items = pedido.getDetalles().stream()
                    .map(detalle -> new ItemOrdenDTO(
                            detalle.getProductoId(), // Ahora usa getProductoId() directamente
                            detalle.getCantidad()))
                    .collect(Collectors.toList());

            // Crear la orden ejecutada
            OrdenEjecutadaDTO ordenEjecutada = new OrdenEjecutadaDTO(
                    pedido.getId(),
                    "EJECUTADA",
                    items);

            // Enviar mensaje a RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDEN_EJECUTADA_EXCHANGE,
                    RabbitMQConfig.ORDEN_EJECUTADA_ROUTING_KEY,
                    ordenEjecutada);

            log.info("Orden de actualización de stock enviada para pedido: {}", pedido.getNumeroPedido());
            return true;

        } catch (Exception e) {
            log.error("Error al enviar orden de actualización de stock para pedido {}: {}",
                    pedido.getNumeroPedido(), e.getMessage());
            return false;
        }
    }

    private String generarNumeroPedido() {
        // Generar número de pedido único basado en fecha y hora
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PED-" + timestamp;
    }

    public BigDecimal calcularMontoPendienteCliente(Long clienteId) {
        try {
            // Estados que comprometen el saldo del cliente
            List<Pedido.EstadoPedido> estadosPendientes = Arrays.asList(
                    Pedido.EstadoPedido.ACEPTADO,
                    Pedido.EstadoPedido.EN_PREPARACION);

            List<Pedido> pedidosPendientes = pedidoRepository.findByClienteIdAndEstadoIn(clienteId, estadosPendientes);

            BigDecimal montoTotal = pedidosPendientes.stream()
                    .map(Pedido::getMontoTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Cliente {}: Monto comprometido en pedidos pendientes: {}", clienteId, montoTotal);
            return montoTotal;

        } catch (Exception e) {
            log.error("Error al calcular monto pendiente para cliente {}: {}", clienteId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    public Pedido actualizarEstadoPedido(String pedidoId, Pedido.EstadoPedido nuevoEstado) {
        log.info("Actualizando estado del pedido {} a {}", pedidoId, nuevoEstado);

        // Buscar el pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        // Validar transiciones de estado permitidas
        if (!esTransicionEstadoValida(pedido.getEstado(), nuevoEstado)) {
            throw new IllegalStateException(
                    String.format("No se puede cambiar el estado de %s a %s",
                            pedido.getEstado(), nuevoEstado));
        }

        Pedido.EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);

        // Si el pedido se cancela, devolver el stock
        if (nuevoEstado == Pedido.EstadoPedido.CANCELADO) {
            procesarCancelacionPedido(pedido, estadoAnterior);
        }

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        log.info("Pedido {} actualizado de {} a {}",
                pedido.getNumeroPedido(), estadoAnterior, nuevoEstado);

        return pedidoActualizado;
    }

    private boolean esTransicionEstadoValida(Pedido.EstadoPedido estadoActual, Pedido.EstadoPedido nuevoEstado) {
        // Solo permitir actualizar a ENTREGADO o CANCELADO desde ciertos estados
        switch (nuevoEstado) {
            case ENTREGADO:
                return estadoActual == Pedido.EstadoPedido.EN_PREPARACION;
            case CANCELADO:
                return estadoActual == Pedido.EstadoPedido.ACEPTADO ||
                        estadoActual == Pedido.EstadoPedido.EN_PREPARACION;
            default:
                return false;
        }
    }

    private void procesarCancelacionPedido(Pedido pedido, Pedido.EstadoPedido estadoAnterior) {
        log.info("Procesando cancelación del pedido {}", pedido.getNumeroPedido());

        // Solo devolver stock si el pedido había descontado stock previamente
        // (es decir, si estaba en EN_PREPARACION o estados posteriores)
        if (estadoAnterior == Pedido.EstadoPedido.EN_PREPARACION) {

            enviarMensajeDevolucionStock(pedido);
        } else {
            log.info("Pedido {} cancelado pero no requiere devolución de stock (estado anterior: {})",
                    pedido.getNumeroPedido(), estadoAnterior);
        }
    }

    private void enviarMensajeDevolucionStock(Pedido pedido) {
        try {
            // Crear lista de items para devolución
            List<StockDevolucionDTO.ItemDevolucionDTO> items = pedido.getDetalles().stream()
                    .map(detalle -> new StockDevolucionDTO.ItemDevolucionDTO(
                            detalle.getProductoId(), // Ahora usa getProductoId() directamente
                            detalle.getCantidad()))
                    .collect(Collectors.toList());

            // Crear el DTO de devolución
            StockDevolucionDTO devolucion = new StockDevolucionDTO(
                    pedido.getId(),
                    pedido.getNumeroPedido(),
                    "Pedido cancelado",
                    items);

            // Enviar mensaje a RabbitMQ
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STOCK_DEVOLUCION_EXCHANGE,
                    RabbitMQConfig.STOCK_DEVOLUCION_ROUTING_KEY,
                    devolucion);

            log.info("Mensaje de devolución de stock enviado para pedido: {}", pedido.getNumeroPedido());

        } catch (Exception e) {
            log.error("Error al enviar mensaje de devolución de stock para pedido {}: {}",
                    pedido.getNumeroPedido(), e.getMessage());
            // Nota: Podrías implementar un mecanismo de retry o alertas aquí
        }
    }

    public List<Pedido> getAllPedidos(Long userId, Long clienteId, Pedido.EstadoPedido estado) {
        // Si hay userId, obtener los clientes de ese usuario
        if (userId != null) {
            List<Long> clienteIds = clienteService.obtenerClientesPorUsuario(userId);
            
            if (clienteIds.isEmpty()) {
                log.info("No se encontraron clientes para el usuario: {}", userId);
                return new ArrayList<>();
            }
            
            // Si también se especificó un clienteId, validar que pertenezca al usuario
            if (clienteId != null) {
                if (!clienteIds.contains(clienteId)) {
                    log.warn("El cliente {} no pertenece al usuario {}", clienteId, userId);
                    return new ArrayList<>();
                }
                // Si el cliente pertenece al usuario, filtrar por ese cliente específico
                if (estado != null) {
                    log.info("Filtrando pedidos por userId: {}, clienteId: {} y estado: {}", userId, clienteId, estado);
                    return pedidoRepository.findByClienteIdAndEstado(clienteId, estado);
                } else {
                    log.info("Filtrando pedidos por userId: {} y clienteId: {}", userId, clienteId);
                    return pedidoRepository.findByClienteId(clienteId);
                }
            }
            
            // Filtrar por lista de clientes del usuario
            if (estado != null) {
                log.info("Filtrando pedidos por userId: {} ({} clientes) y estado: {}", userId, clienteIds.size(), estado);
                return pedidoRepository.findByClienteIdInAndEstado(clienteIds, estado);
            } else {
                log.info("Filtrando pedidos por userId: {} ({} clientes)", userId, clienteIds.size());
                return pedidoRepository.findByClienteIdIn(clienteIds);
            }
        }
        
        // Si no hay userId, usar la lógica anterior
        // Si ambos parámetros son null, devolver todos
        if (clienteId == null && estado == null) {
            log.info("Obteniendo todos los pedidos sin filtros");
            return pedidoRepository.findAll();
        }
        
        // Si solo hay clienteId
        if (clienteId != null && estado == null) {
            log.info("Filtrando pedidos por clienteId: {}", clienteId);
            return pedidoRepository.findByClienteId(clienteId);
        }
        
        // Si solo hay estado
        if (clienteId == null && estado != null) {
            log.info("Filtrando pedidos por estado: {}", estado);
            return pedidoRepository.findByEstado(estado);
        }
        
        // Si hay ambos parámetros
        log.info("Filtrando pedidos por clienteId: {} y estado: {}", clienteId, estado);
        return pedidoRepository.findByClienteIdAndEstado(clienteId, estado);
    }

    public Pedido getPedidoById(String id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public void deletePedido(String id) {
        pedidoRepository.deleteById(id);
    }
}