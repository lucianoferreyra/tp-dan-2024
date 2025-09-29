package isi.dan.ms.pedidos.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isi.dan.ms.pedidos.dto.ProductoDTO;
import isi.dan.ms.pedidos.dto.StockUpdateDTO;
import java.util.List;

@Service
public class ProductoService {

  private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

  @Autowired
  private RestTemplate restTemplate;

  @Value("${ms.productos.url:http://localhost:8082}")
  private String productosServiceUrl;

  public ProductoDTO obtenerProducto(Long productoId) {
    try {
      String url = productosServiceUrl + "/api/productos/" + productoId;
      log.info("Consultando producto en URL: {}", url);

      ProductoDTO producto = restTemplate.getForObject(url, ProductoDTO.class);
      log.info("Producto encontrado: {}", producto != null ? producto.getId() : "null");

      return producto;
    } catch (HttpClientErrorException.NotFound e) {
      log.error("Producto no encontrado con ID: {}", productoId);
      throw new RuntimeException("Producto no encontrado con ID: " + productoId);
    } catch (Exception e) {
      log.error("Error al consultar producto: {}", e.getMessage());
      throw new RuntimeException("Error al comunicarse con el servicio de productos", e);
    }
  }

  public boolean verificarStockDisponible(Long productoId, Integer cantidadRequerida) {
    try {
      String url = productosServiceUrl + "/api/productos/" + productoId + "/stock/" + cantidadRequerida;
      log.info("Verificando stock del producto {} para cantidad: {}", productoId, cantidadRequerida);

      Boolean tieneStock = restTemplate.getForObject(url, Boolean.class);
      log.info("Producto {} tiene stock suficiente: {}", productoId, tieneStock);

      return tieneStock != null ? tieneStock : false;
    } catch (HttpClientErrorException.NotFound e) {
      log.error("Producto no encontrado para verificación de stock: {}", productoId);
      return false;
    } catch (Exception e) {
      log.error("Error al verificar stock del producto: {}", e.getMessage());
      return false;
    }
  }

  public boolean actualizarStock(List<StockUpdateDTO> stockUpdates) {
    try {
      String url = productosServiceUrl + "/api/productos/stock/actualizar";
      log.info("Actualizando stock para {} productos", stockUpdates.size());

      Boolean actualizado = restTemplate.postForObject(url, stockUpdates, Boolean.class);
      log.info("Stock actualizado correctamente: {}", actualizado);

      return actualizado != null ? actualizado : false;
    } catch (Exception e) {
      log.error("Error al actualizar stock: {}", e.getMessage());
      return false;
    }
  }
}