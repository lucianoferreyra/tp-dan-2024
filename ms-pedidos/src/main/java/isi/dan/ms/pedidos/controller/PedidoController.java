package isi.dan.ms.pedidos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isi.dan.ms.pedidos.dto.CrearPedidoDTO;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.servicio.PedidoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody CrearPedidoDTO crearPedidoDTO) {
        try {
            Pedido pedido = pedidoService.savePedido(crearPedidoDTO);
            return new ResponseEntity<>(pedido, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public List<Pedido> getAllPedidos() {
        return pedidoService.getAllPedidos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable String id) {
        Pedido pedido = pedidoService.getPedidoById(id);
        return pedido != null ? ResponseEntity.ok(pedido) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable String id) {
        pedidoService.deletePedido(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}/monto-pendiente")
    public ResponseEntity<BigDecimal> obtenerMontoPendienteCliente(@PathVariable Long clienteId) {
        try {
            BigDecimal montoPendiente = pedidoService.calcularMontoPendienteCliente(clienteId);
            return ResponseEntity.ok(montoPendiente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BigDecimal.ZERO);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstadoPedido(
            @PathVariable String id,
            @RequestBody Map<String, Pedido.EstadoPedido> request) {
        try {
            Pedido.EstadoPedido nuevoEstado = request.get("nuevoEstado");
            Pedido pedidoActualizado = pedidoService.actualizarEstadoPedido(id, nuevoEstado);
            if (pedidoActualizado != null) {
                return ResponseEntity.ok(pedidoActualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
