package isi.dan.ms_productos.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.ms_productos.conf.RabbitMQConfig;
import isi.dan.ms_productos.dto.ItemOrdenDTO;
import isi.dan.ms_productos.dto.OrdenEjecutadaDTO;
import isi.dan.ms_productos.dto.StockDevolucionDTO;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Producto;

@Service
public class StockService {

  @Autowired
  private ProductoService productoService;

  @Autowired
  private ObjectMapper objectMapper;

  Logger log = LoggerFactory.getLogger(StockService.class);

  @RabbitListener(queues = RabbitMQConfig.ORDEN_EJECUTADA_QUEUE)
  @Transactional
  public void procesarOrdenEjecutada(String mensaje) {
    log.info("Recibido mensaje de orden ejecutada: {}", mensaje);

    try {
      // Deserializar el mensaje
      OrdenEjecutadaDTO ordenEjecutada = objectMapper.readValue(mensaje, OrdenEjecutadaDTO.class);
      log.info("Procesando orden ejecutada ID: {}", ordenEjecutada.getOrdenId());

      // Verificar que la orden esté en estado ejecutada
      if (!"EJECUTADA".equals(ordenEjecutada.getEstado()) &&
          !"CONFIRMADA".equals(ordenEjecutada.getEstado())) {
        log.warn("Orden {} no está en estado EJECUTADA o CONFIRMADA, estado actual: {}",
            ordenEjecutada.getOrdenId(), ordenEjecutada.getEstado());
        return;
      }

      // Procesar cada item de la orden
      for (ItemOrdenDTO item : ordenEjecutada.getItems()) {
        try {
          actualizarStockProducto(item.getProductoId(), item.getCantidad());
          log.info("Stock actualizado para producto ID: {}, cantidad descontada: {}",
              item.getProductoId(), item.getCantidad());
        } catch (ProductoNotFoundException e) {
          log.error("Producto no encontrado ID: {} en orden {}",
              item.getProductoId(), ordenEjecutada.getOrdenId());
          // Continuar con los otros productos
        } catch (IllegalArgumentException e) {
          log.error("Stock insuficiente para producto ID: {} en orden {}: {}",
              item.getProductoId(), ordenEjecutada.getOrdenId(), e.getMessage());
          // Continuar con los otros productos
        }
      }

      log.info("Orden {} procesada exitosamente", ordenEjecutada.getOrdenId());

    } catch (Exception e) {
      log.error("Error procesando mensaje de orden ejecutada: {}", e.getMessage(), e);
    }
  }

  @Transactional
  public void actualizarStockProducto(Long productoId, Integer cantidadVendida)
      throws ProductoNotFoundException {

    log.info("Actualizando stock del producto ID: {}, cantidad a descontar: {}",
        productoId, cantidadVendida);

    // Obtener el producto
    Producto producto = productoService.getProductoById(productoId);

    // Verificar stock suficiente
    if (producto.getStockActual() < cantidadVendida) {
      throw new IllegalArgumentException(
          String.format("Stock insuficiente para producto %s. Stock actual: %d, cantidad solicitada: %d",
              producto.getNombre(), producto.getStockActual(), cantidadVendida));
    }

    // Descontar del stock
    int stockAnterior = producto.getStockActual();
    int nuevoStock = stockAnterior - cantidadVendida;
    producto.setStockActual(nuevoStock);

    // Guardar cambios
    productoService.saveProducto(producto);

    log.info("Stock actualizado para producto {} (ID: {}): {} -> {}",
        producto.getNombre(), productoId, stockAnterior, nuevoStock);

    // Verificar punto de reorden
    verificarPuntoReorden(producto);
  }

  private void verificarPuntoReorden(Producto producto) {
    if (producto.getStockActual() <= producto.getStockMinimo()) {
      log.warn("ALERTA: Producto {} (ID: {}) ha alcanzado el punto de reorden. Stock actual: {}, Stock mínimo: {}",
          producto.getNombre(), producto.getId(), producto.getStockActual(), producto.getStockMinimo());

      // Aquí podrías enviar un mensaje a otro servicio para generar una orden de
      // compra
      // o implementar la lógica de reposición automática
      enviarAlertaStockMinimo(producto);
    }
  }

  private void enviarAlertaStockMinimo(Producto producto) {
    // TODO: Implementar envío de alerta (email, mensaje a cola, etc.)
    log.info("Enviando alerta de stock mínimo para producto: {}", producto.getNombre());
  }

  // Método para rollback manual en caso de error
  @Transactional
  public void restaurarStock(Long productoId, Integer cantidad) throws ProductoNotFoundException {
    log.info("Restaurando stock del producto ID: {}, cantidad a restaurar: {}", productoId, cantidad);

    Producto producto = productoService.getProductoById(productoId);
    int stockAnterior = producto.getStockActual();
    int nuevoStock = stockAnterior + cantidad;
    producto.setStockActual(nuevoStock);

    productoService.saveProducto(producto);

    log.info("Stock restaurado para producto {} (ID: {}): {} -> {}",
        producto.getNombre(), productoId, stockAnterior, nuevoStock);
  }

  @RabbitListener(queues = RabbitMQConfig.STOCK_DEVOLUCION_QUEUE)
  @Transactional
  public void procesarDevolucionStock(String mensaje) {
    log.info("Recibido mensaje de devolución de stock: {}", mensaje);

    try {
      // Parsear el mensaje JSON
      StockDevolucionDTO devolucion = objectMapper.readValue(mensaje, StockDevolucionDTO.class);

      log.info("Procesando devolución de stock para pedido: {}", devolucion.getNumeroPedido());

      // Devolver stock para cada item
      for (StockDevolucionDTO.ItemDevolucionDTO item : devolucion.getItems()) {
        restaurarStockProducto(item.getProductoId(), item.getCantidad());
      }

      log.info("Devolución de stock completada para pedido: {}", devolucion.getNumeroPedido());

    } catch (Exception e) {
      log.error("Error procesando devolución de stock: {}", e.getMessage());
      // Implementar lógica de manejo de errores (retry, dead letter queue, etc.)
    }
  }

  @Transactional
  public void restaurarStockProducto(Long productoId, Integer cantidadARestaurar)
      throws ProductoNotFoundException {

    log.info("Restaurando stock del producto ID: {}, cantidad a restaurar: {}",
        productoId, cantidadARestaurar);

    // Obtener el producto
    Producto producto = productoService.getProductoById(productoId);

    // Restaurar el stock
    int stockAnterior = producto.getStockActual();
    int nuevoStock = stockAnterior + cantidadARestaurar;
    producto.setStockActual(nuevoStock);

    // Guardar cambios
    productoService.saveProducto(producto);

    log.info("Stock restaurado para producto {} (ID: {}): {} -> {} (restaurado: {})",
        producto.getNombre(), productoId, stockAnterior, nuevoStock, cantidadARestaurar);
  }
}