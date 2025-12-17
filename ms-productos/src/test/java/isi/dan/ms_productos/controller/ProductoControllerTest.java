package isi.dan.ms_productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import isi.dan.ms_productos.dto.DescuentoPromocionalDTO;
import isi.dan.ms_productos.dto.OrdenProvisionDTO;
import isi.dan.ms_productos.dto.ProductoCreateDTO;
import isi.dan.ms_productos.dto.ProductoUpdateDTO;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.modelo.Producto;
import isi.dan.ms_productos.servicio.EchoClientFeign;
import isi.dan.ms_productos.servicio.ProductoService;
import isi.dan.ms_productos.servicio.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private StockService stockService;

    @MockBean
    private EchoClientFeign echoClientFeign;

    private Producto producto;
    private Categoria categoria;
    private ProductoCreateDTO productoCreateDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Placas");
        categoria.setId(1L);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Placa de Yeso");
        producto.setDescripcion("Placa de yeso estándar 12.5mm");
        producto.setPrecio(new BigDecimal("850.00"));
        producto.setStockActual(10);
        producto.setStockMinimo(5);
        producto.setCategoria(categoria);
        producto.setDescuentoPromocional(BigDecimal.ZERO);

        productoCreateDTO = new ProductoCreateDTO(
                "Placa de Yeso",
                "Placa de yeso estándar 12.5mm",
                1L,
                5,
                new BigDecimal("850.00"),
                BigDecimal.ZERO
        );
    }

    @Test
    void testCreateProducto_Success() throws Exception {
        when(productoService.saveProducto(any(ProductoCreateDTO.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Placa de Yeso")))
                .andExpect(jsonPath("$.descripcion", is("Placa de yeso estándar 12.5mm")))
                .andExpect(jsonPath("$.precio", is(850.00)))
                .andExpect(jsonPath("$.stockActual", is(10)))
                .andExpect(jsonPath("$.stockMinimo", is(5)));

        verify(productoService, times(1)).saveProducto(any(ProductoCreateDTO.class));
    }

    @Test
    void testCreateProducto_CategoriaNotFound() throws Exception {
        when(productoService.saveProducto(any(ProductoCreateDTO.class)))
                .thenThrow(new CategoriaNotFoundException(1L));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoCreateDTO)))
                .andExpect(status().isBadRequest());

        verify(productoService, times(1)).saveProducto(any(ProductoCreateDTO.class));
    }

    @Test
    void testGetAllProductos_WithoutFilters() throws Exception {
        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Cemento Portland");
        producto2.setDescripcion("Cemento Portland tipo CPN-40");
        producto2.setPrecio(new BigDecimal("450.00"));
        producto2.setStockActual(50);
        producto2.setStockMinimo(10);
        producto2.setCategoria(categoria);

        List<Producto> productos = Arrays.asList(producto, producto2);

        when(productoService.getAllProductos(null, null, null, null, null))
                .thenReturn(productos);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Placa de Yeso")))
                .andExpect(jsonPath("$[1].nombre", is("Cemento Portland")));

        verify(productoService, times(1)).getAllProductos(null, null, null, null, null);
    }

    @Test
    void testGetAllProductos_WithFilters() throws Exception {
        List<Producto> productos = Arrays.asList(producto);

        when(productoService.getAllProductos(eq("Placa de Yeso"), eq(new BigDecimal("500")),
                eq(new BigDecimal("1000")), eq(5), eq(20)))
                .thenReturn(productos);

        mockMvc.perform(get("/api/productos")
                        .param("nombre", "Placa de Yeso")
                        .param("precioMin", "500")
                        .param("precioMax", "1000")
                        .param("stockMin", "5")
                        .param("stockMax", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Placa de Yeso")));

        verify(productoService, times(1))
                .getAllProductos(eq("Placa de Yeso"), eq(new BigDecimal("500")),
                        eq(new BigDecimal("2000")), eq(5), eq(20));
    }

    @Test
    void testGetProductoById_Success() throws Exception {
        when(productoService.getProductoById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Placa de Yeso")))
                .andExpect(jsonPath("$.precio", is(850.00)));

        verify(productoService, times(1)).getProductoById(1L);
    }

    @Test
    void testGetProductoById_NotFound() throws Exception {
        when(productoService.getProductoById(999L))
                .thenThrow(new ProductoNotFoundException(999L));

        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).getProductoById(999L);
    }

    @Test
    void testUpdateProducto_Success() throws Exception {
        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("Placa de Yeso Reforzada");
        updateDTO.setDescripcion("Placa de yeso resistente a la humedad");
        updateDTO.setPrecio(new BigDecimal("950.00"));
        updateDTO.setDescuentoPromocional(new BigDecimal("5.00"));
        updateDTO.setStockMinimo(6);
        updateDTO.setCategoriaId(1L);

        Producto updatedProducto = new Producto();
        updatedProducto.setId(1L);
        updatedProducto.setNombre("Placa de Yeso Reforzada");
        updatedProducto.setDescripcion("Placa de yeso resistente a la humedad");
        updatedProducto.setPrecio(new BigDecimal("950.00"));
        updatedProducto.setStockMinimo(6);
        updatedProducto.setCategoria(categoria);

        when(productoService.updateProducto(eq(1L), any(ProductoUpdateDTO.class)))
                .thenReturn(updatedProducto);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Placa de Yeso Reforzada")))
                .andExpect(jsonPath("$.descripcion", is("Placa de yeso resistente a la humedad")))
                .andExpect(jsonPath("$.precio", is(950.00)));

        verify(productoService, times(1)).updateProducto(eq(1L), any(ProductoUpdateDTO.class));
    }

    @Test
    void testUpdateProducto_NotFound() throws Exception {
        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("Placa de Yeso Reforzada");
        updateDTO.setPrecio(new BigDecimal("950.00"));
        updateDTO.setDescuentoPromocional(new BigDecimal("5.00"));
        updateDTO.setStockMinimo(6);
        updateDTO.setCategoriaId(1L);

        when(productoService.updateProducto(eq(999L), any(ProductoUpdateDTO.class)))
                .thenThrow(new ProductoNotFoundException(999L));

        mockMvc.perform(put("/api/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).updateProducto(eq(999L), any(ProductoUpdateDTO.class));
    }

    @Test
    void testUpdateProducto_CategoriaNotFound() throws Exception {
        ProductoUpdateDTO updateDTO = new ProductoUpdateDTO();
        updateDTO.setNombre("Placa de Yeso Reforzada");
        updateDTO.setPrecio(new BigDecimal("950.00"));
        updateDTO.setDescuentoPromocional(new BigDecimal("5.00"));
        updateDTO.setStockMinimo(6);
        updateDTO.setCategoriaId(999L);

        when(productoService.updateProducto(eq(1L), any(ProductoUpdateDTO.class)))
                .thenThrow(new CategoriaNotFoundException(999L));

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());

        verify(productoService, times(1)).updateProducto(eq(1L), any(ProductoUpdateDTO.class));
    }

    @Test
    void testDeleteProducto_Success() throws Exception {
        doNothing().when(productoService).deleteProducto(1L);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).deleteProducto(1L);
    }

    @Test
    void testDeleteProducto_NotFound() throws Exception {
        doThrow(new ProductoNotFoundException(999L)).when(productoService).deleteProducto(999L);

        mockMvc.perform(delete("/api/productos/999"))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).deleteProducto(999L);
    }

    @Test
    void testProcesarOrdenProvision_Success() throws Exception {
        OrdenProvisionDTO ordenDTO = new OrdenProvisionDTO(1L, 20, new BigDecimal("800.00"));

        Producto productoActualizado = new Producto();
        productoActualizado.setId(1L);
        productoActualizado.setNombre("Placa de Yeso");
        productoActualizado.setStockActual(30);

        when(productoService.procesarOrdenProvision(any(OrdenProvisionDTO.class)))
                .thenReturn(productoActualizado);

        mockMvc.perform(put("/api/productos/provision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordenDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.stockActual", is(30)));

        verify(productoService, times(1)).procesarOrdenProvision(any(OrdenProvisionDTO.class));
    }

    @Test
    void testProcesarOrdenProvision_ProductoNotFound() throws Exception {
        OrdenProvisionDTO ordenDTO = new OrdenProvisionDTO(999L, 20, new BigDecimal("800.00"));

        when(productoService.procesarOrdenProvision(any(OrdenProvisionDTO.class)))
                .thenThrow(new ProductoNotFoundException(999L));

        mockMvc.perform(put("/api/productos/provision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordenDTO)))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).procesarOrdenProvision(any(OrdenProvisionDTO.class));
    }

    @Test
    void testActualizarDescuentoPromocional_Success() throws Exception {
        DescuentoPromocionalDTO descuentoDTO = new DescuentoPromocionalDTO(new BigDecimal("10.00"));

        Producto productoConDescuento = new Producto();
        productoConDescuento.setId(1L);
        productoConDescuento.setNombre("Placa de Yeso");
        productoConDescuento.setPrecio(new BigDecimal("850.00"));
        productoConDescuento.setDescuentoPromocional(new BigDecimal("10.00"));

        when(productoService.actualizarDescuentoPromocional(eq(1L), any(DescuentoPromocionalDTO.class)))
                .thenReturn(productoConDescuento);

        mockMvc.perform(put("/api/productos/1/descuento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuentoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.descuentoPromocional", is(10.00)));

        verify(productoService, times(1))
                .actualizarDescuentoPromocional(eq(1L), any(DescuentoPromocionalDTO.class));
    }

    @Test
    void testActualizarDescuentoPromocional_NotFound() throws Exception {
        DescuentoPromocionalDTO descuentoDTO = new DescuentoPromocionalDTO(new BigDecimal("10.00"));

        when(productoService.actualizarDescuentoPromocional(eq(999L), any(DescuentoPromocionalDTO.class)))
                .thenThrow(new ProductoNotFoundException(999L));

        mockMvc.perform(put("/api/productos/999/descuento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuentoDTO)))
                .andExpect(status().isNotFound());

        verify(productoService, times(1))
                .actualizarDescuentoPromocional(eq(999L), any(DescuentoPromocionalDTO.class));
    }

    @Test
    void testVerificarStockDisponible_True() throws Exception {
        when(productoService.getProductoById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1/stock/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(productoService, times(1)).getProductoById(1L);
    }

    @Test
    void testVerificarStockDisponible_False() throws Exception {
        when(productoService.getProductoById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1/stock/20"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(productoService, times(1)).getProductoById(1L);
    }

    @Test
    void testVerificarStockDisponible_ProductoNotFound() throws Exception {
        when(productoService.getProductoById(999L))
                .thenThrow(new ProductoNotFoundException(999L));

        mockMvc.perform(get("/api/productos/999/stock/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(productoService, times(1)).getProductoById(999L);
    }

    @Test
    void testActualizarStock_Success() throws Exception {
        StockUpdateDTO update1 = new StockUpdateDTO();
        update1.setIdProducto(1L);
        update1.setCantidad(-5);

        StockUpdateDTO update2 = new StockUpdateDTO();
        update2.setIdProducto(2L);
        update2.setCantidad(-3);

        List<StockUpdateDTO> stockUpdates = Arrays.asList(update1, update2);

        doNothing().when(stockService).actualizarStockProducto(anyLong(), anyInt());

        mockMvc.perform(post("/api/productos/stock/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUpdates)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(stockService, times(2)).actualizarStockProducto(anyLong(), anyInt());
    }

    @Test
    void testActualizarStock_Error() throws Exception {
        StockUpdateDTO update = new StockUpdateDTO();
        update.setIdProducto(1L);
        update.setCantidad(-5);

        List<StockUpdateDTO> stockUpdates = Arrays.asList(update);

        doThrow(new RuntimeException("Error al actualizar stock"))
                .when(stockService).actualizarStockProducto(anyLong(), anyInt());

        mockMvc.perform(post("/api/productos/stock/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUpdates)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(stockService, times(1)).actualizarStockProducto(anyLong(), anyInt());
    }

    @Test
    void testGetEcho() throws Exception {
        when(echoClientFeign.echo()).thenReturn("Echo response");

        mockMvc.perform(get("/api/productos/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Echo response"));

        verify(echoClientFeign, times(1)).echo();
    }
}
