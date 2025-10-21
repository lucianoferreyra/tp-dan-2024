package isi.dan.ms_productos.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.ms_productos.conf.RabbitMQConfig;
import isi.dan.ms_productos.dao.ProductoRepository;
import isi.dan.ms_productos.dto.DescuentoPromocionalDTO;
import isi.dan.ms_productos.dto.OrdenProvisionDTO;
import isi.dan.ms_productos.dto.ProductoCreateDTO;
import isi.dan.ms_productos.dto.ProductoUpdateDTO;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.modelo.Producto;

import java.util.List;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaService categoriaService;

    Logger log = LoggerFactory.getLogger(ProductoService.class);

    @RabbitListener(queues = RabbitMQConfig.STOCK_UPDATE_QUEUE)
    public void handleStockUpdate(Message msg) {
        log.info("Recibido {}", msg);
        // buscar el producto
        // actualizar el stock
        // verificar el punto de pedido y generar un pedido
    }

    public Producto saveProducto(ProductoCreateDTO productoCreateDTO) throws CategoriaNotFoundException {
        log.info("Creando producto: {}", productoCreateDTO.getNombre());

        // Buscar la categoría
        Categoria categoria = categoriaService.getCategoriaById(productoCreateDTO.getCategoriaId());

        // Crear el producto con stock inicial 0
        Producto producto = new Producto(
                productoCreateDTO.getNombre(),
                productoCreateDTO.getDescripcion(),
                categoria,
                productoCreateDTO.getStockMinimo(),
                productoCreateDTO.getPrecio(),
                productoCreateDTO.getDescuentoPromocional());

        log.info("Producto creado con stock inicial 0: {}", producto);
        return productoRepository.save(producto);
    }

    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto procesarOrdenProvision(OrdenProvisionDTO ordenProvisionDTO) throws ProductoNotFoundException {
        log.info("Procesando orden de provisión: {}", ordenProvisionDTO);

        // Buscar el producto
        Producto producto = getProductoById(ordenProvisionDTO.getIdProducto());

        // Actualizar stock (sumar la cantidad recibida)
        int nuevoStock = producto.getStockActual() + ordenProvisionDTO.getCantidadRecibida();
        producto.setStockActual(nuevoStock);

        // Actualizar precio
        producto.setPrecio(ordenProvisionDTO.getPrecio());

        log.info("Stock actualizado de {} a {} para producto ID: {}",
                producto.getStockActual() - ordenProvisionDTO.getCantidadRecibida(),
                nuevoStock,
                producto.getId());
        log.info("Precio actualizado a {} para producto ID: {}",
                ordenProvisionDTO.getPrecio(),
                producto.getId());

        return productoRepository.save(producto);
    }

    public Producto actualizarDescuentoPromocional(Long id, DescuentoPromocionalDTO descuentoDTO)
            throws ProductoNotFoundException {
        log.info("Actualizando descuento promocional para producto ID: {} - Nuevo descuento: {}%",
                id, descuentoDTO.getDescuentoPromocional());

        // Buscar el producto
        Producto producto = getProductoById(id);

        // Actualizar descuento promocional
        producto.setDescuentoPromocional(descuentoDTO.getDescuentoPromocional());

        return productoRepository.save(producto);
    }

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) throws ProductoNotFoundException {
        return productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
    }

    public void deleteProducto(Long id) throws ProductoNotFoundException {
        log.info("Eliminando producto del catálogo con ID: {}", id);
        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException(id);
        }
        productoRepository.deleteById(id);
        log.info("Producto eliminado del catálogo con ID: {}", id);
    }

    public Producto updateProducto(Long id, ProductoUpdateDTO productoDTO)
            throws ProductoNotFoundException, CategoriaNotFoundException {
        log.info("Actualizando producto con ID: {}", id);
        Producto existingProducto = getProductoById(id);

        // Buscar la categoría
        Categoria categoria = categoriaService.getCategoriaById(productoDTO.getCategoriaId());

        existingProducto.setNombre(productoDTO.getNombre());
        existingProducto.setDescripcion(productoDTO.getDescripcion());
        existingProducto.setPrecio(productoDTO.getPrecio());
        existingProducto.setDescuentoPromocional(productoDTO.getDescuentoPromocional());
        existingProducto.setStockMinimo(productoDTO.getStockMinimo());
        existingProducto.setCategoria(categoria);

        return productoRepository.save(existingProducto);
    }
}
