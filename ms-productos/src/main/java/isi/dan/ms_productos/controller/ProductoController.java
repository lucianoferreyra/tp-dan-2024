package isi.dan.ms_productos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import isi.dan.ms_productos.aop.LogExecutionTime;
import isi.dan.ms_productos.dto.DescuentoPromocionalDTO;
import isi.dan.ms_productos.dto.OrdenProvisionDTO;
import isi.dan.ms_productos.dto.ProductoCreateDTO;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Producto;
import isi.dan.ms_productos.servicio.EchoClientFeign;
import isi.dan.ms_productos.servicio.ProductoService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    Logger log = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    EchoClientFeign echoSvc;

    @PostMapping
    @LogExecutionTime
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody ProductoCreateDTO productoCreateDTO) {
        log.info("POST /api/productos - Creando producto: {}", productoCreateDTO.getNombre());
        try {
            Producto savedProducto = productoService.saveProducto(productoCreateDTO);
            log.info("Producto creado exitosamente con ID: {}", savedProducto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProducto);
        } catch (CategoriaNotFoundException e) {
            log.warn("Categoría no encontrada con ID: {}", productoCreateDTO.getCategoriaId());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/provision")
    @LogExecutionTime
    public ResponseEntity<Producto> procesarOrdenProvision(@Valid @RequestBody OrdenProvisionDTO ordenProvisionDTO) {
        log.info("PUT /api/productos/provision - Procesando orden de provisión: {}", ordenProvisionDTO);
        try {
            Producto productoActualizado = productoService.procesarOrdenProvision(ordenProvisionDTO);
            log.info("Orden de provisión procesada exitosamente para producto ID: {}", productoActualizado.getId());
            return ResponseEntity.ok(productoActualizado);
        } catch (ProductoNotFoundException e) {
            log.warn("Producto no encontrado con ID: {}", ordenProvisionDTO.getIdProducto());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/descuento")
    @LogExecutionTime
    public ResponseEntity<Producto> actualizarDescuentoPromocional(@PathVariable Long id,
            @Valid @RequestBody DescuentoPromocionalDTO descuentoDTO) {
        log.info("PUT /api/productos/{}/descuento - Actualizando descuento promocional: {}%",
                id, descuentoDTO.getDescuentoPromocional());
        try {
            Producto productoActualizado = productoService.actualizarDescuentoPromocional(id, descuentoDTO);
            log.info(
                    "Descuento promocional actualizado exitosamente para producto ID: {} - Nuevo precio con descuento: {}",
                    productoActualizado.getId(), productoActualizado.getPrecioConDescuento());
            return ResponseEntity.ok(productoActualizado);
        } catch (ProductoNotFoundException e) {
            log.warn("Producto no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test")
    @LogExecutionTime
    public String getEcho() {
        String resultado = echoSvc.echo();
        log.info("Log en test 1!!!! {}", resultado);
        return resultado;
    }

    @GetMapping("/test2")
    @LogExecutionTime
    public String getEcho2() {
        RestTemplate restTemplate = new RestTemplate();
        String gatewayURL = "http://ms-gateway-svc:8080";
        String resultado = restTemplate.getForObject(gatewayURL + "/clientes/api/clientes/echo", String.class);
        log.info("Log en test 2 {}", resultado);
        return resultado;
    }

    @GetMapping
    @LogExecutionTime
    public ResponseEntity<List<Producto>> getAllProductos() {
        log.info("GET /api/productos");
        List<Producto> productos = productoService.getAllProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        try {
            Producto producto = productoService.getProductoById(id);
            return ResponseEntity.ok(producto);
        } catch (ProductoNotFoundException e) {
            log.warn("Producto no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        log.info("PUT /api/productos/{}", id);
        try {
            Producto updatedProducto = productoService.updateProducto(id, producto);
            return ResponseEntity.ok(updatedProducto);
        } catch (ProductoNotFoundException e) {
            log.warn("Producto no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        log.info("DELETE /api/productos/{} - Eliminando del catálogo", id);
        try {
            productoService.deleteProducto(id);
            log.info("Producto eliminado del catálogo con ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ProductoNotFoundException e) {
            log.warn("Producto no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
