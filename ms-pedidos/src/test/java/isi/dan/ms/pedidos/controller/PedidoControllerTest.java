package isi.dan.ms.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import isi.dan.ms.pedidos.dto.CrearPedidoDTO;
import isi.dan.ms.pedidos.modelo.DetallePedido;
import isi.dan.ms.pedidos.modelo.Pedido;
import isi.dan.ms.pedidos.servicio.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    private Pedido pedido;
    private CrearPedidoDTO crearPedidoDTO;

    @BeforeEach
    void setUp() {
        // Crear pedido de prueba
        pedido = new Pedido();
        pedido.setId("507f1f77bcf86cd799439011");
        pedido.setNumeroPedido("PED-001");
        pedido.setClienteId(1L);
        pedido.setObraId(1L);
        pedido.setObservaciones("Pedido de prueba");
        pedido.setMontoTotal(new BigDecimal("1500.00"));
        pedido.setEstado(Pedido.EstadoPedido.ACEPTADO);
        pedido.setFechaPedido(LocalDateTime.now());

        DetallePedido detalle = new DetallePedido();
        detalle.setProductoId(1L);
        detalle.setCantidad(10);
        detalle.setPrecioUnitario(new BigDecimal("150.00"));
        detalle.setMontoLinea(new BigDecimal("1500.00"));
        pedido.setDetalles(Arrays.asList(detalle));

        // Crear DTO de prueba
        crearPedidoDTO = new CrearPedidoDTO();
        crearPedidoDTO.setClienteId(1L);
        crearPedidoDTO.setObraId(1L);
        crearPedidoDTO.setObservaciones("Pedido de prueba");

        CrearPedidoDTO.DetallePedidoDTO detalleDTO = new CrearPedidoDTO.DetallePedidoDTO();
        detalleDTO.setProductoId(1L);
        detalleDTO.setCantidad(10);
        crearPedidoDTO.setDetalles(Arrays.asList(detalleDTO));
    }

    @Test
    void testCreatePedido_Success() throws Exception {
        when(pedidoService.savePedido(any(CrearPedidoDTO.class))).thenReturn(pedido);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearPedidoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("507f1f77bcf86cd799439011")))
                .andExpect(jsonPath("$.numeroPedido", is("PED-001")))
                .andExpect(jsonPath("$.clienteId", is(1)))
                .andExpect(jsonPath("$.montoTotal", is(1500.00)))
                .andExpect(jsonPath("$.estado", is("ACEPTADO")));

        verify(pedidoService, times(1)).savePedido(any(CrearPedidoDTO.class));
    }

    @Test
    void testCreatePedido_Error() throws Exception {
        when(pedidoService.savePedido(any(CrearPedidoDTO.class)))
                .thenThrow(new RuntimeException("Error al crear pedido"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearPedidoDTO)))
                .andExpect(status().isBadRequest());

        verify(pedidoService, times(1)).savePedido(any(CrearPedidoDTO.class));
    }

    @Test
    void testGetAllPedidos_WithoutFilters() throws Exception {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.getAllPedidos(null, null, null)).thenReturn(pedidos);

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("507f1f77bcf86cd799439011")))
                .andExpect(jsonPath("$[0].numeroPedido", is("PED-001")));

        verify(pedidoService, times(1)).getAllPedidos(null, null, null);
    }

    @Test
    void testGetAllPedidos_WithClienteIdFilter() throws Exception {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.getAllPedidos(null, 1L, null)).thenReturn(pedidos);

        mockMvc.perform(get("/api/pedidos")
                        .param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].clienteId", is(1)));

        verify(pedidoService, times(1)).getAllPedidos(null, 1L, null);
    }

    @Test
    void testGetAllPedidos_WithEstadoFilter() throws Exception {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.getAllPedidos(null, null, Pedido.EstadoPedido.ACEPTADO))
                .thenReturn(pedidos);

        mockMvc.perform(get("/api/pedidos")
                        .param("estado", "ACEPTADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado", is("ACEPTADO")));

        verify(pedidoService, times(1)).getAllPedidos(null, null, Pedido.EstadoPedido.ACEPTADO);
    }

    @Test
    void testGetAllPedidos_WithAllFilters() throws Exception {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.getAllPedidos(1L, 1L, Pedido.EstadoPedido.ACEPTADO))
                .thenReturn(pedidos);

        mockMvc.perform(get("/api/pedidos")
                        .param("userId", "1")
                        .param("clienteId", "1")
                        .param("estado", "ACEPTADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(pedidoService, times(1)).getAllPedidos(1L, 1L, Pedido.EstadoPedido.ACEPTADO);
    }

    @Test
    void testGetPedidoById_Success() throws Exception {
        when(pedidoService.getPedidoById("507f1f77bcf86cd799439011")).thenReturn(pedido);

        mockMvc.perform(get("/api/pedidos/507f1f77bcf86cd799439011"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("507f1f77bcf86cd799439011")))
                .andExpect(jsonPath("$.numeroPedido", is("PED-001")))
                .andExpect(jsonPath("$.clienteId", is(1)));

        verify(pedidoService, times(1)).getPedidoById("507f1f77bcf86cd799439011");
    }

    @Test
    void testGetPedidoById_NotFound() throws Exception {
        when(pedidoService.getPedidoById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/pedidos/nonexistent"))
                .andExpect(status().isNotFound());

        verify(pedidoService, times(1)).getPedidoById("nonexistent");
    }

    @Test
    void testDeletePedido_Success() throws Exception {
        doNothing().when(pedidoService).deletePedido("507f1f77bcf86cd799439011");

        mockMvc.perform(delete("/api/pedidos/507f1f77bcf86cd799439011"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).deletePedido("507f1f77bcf86cd799439011");
    }

    @Test
    void testObtenerMontoPendienteCliente_Success() throws Exception {
        BigDecimal montoPendiente = new BigDecimal("5000.00");
        when(pedidoService.calcularMontoPendienteCliente(1L)).thenReturn(montoPendiente);

        mockMvc.perform(get("/api/pedidos/cliente/1/monto-pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(5000.00)));

        verify(pedidoService, times(1)).calcularMontoPendienteCliente(1L);
    }

    @Test
    void testObtenerMontoPendienteCliente_Error() throws Exception {
        when(pedidoService.calcularMontoPendienteCliente(1L))
                .thenThrow(new RuntimeException("Error al calcular monto"));

        mockMvc.perform(get("/api/pedidos/cliente/1/monto-pendiente"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$", is(0)));

        verify(pedidoService, times(1)).calcularMontoPendienteCliente(1L);
    }

    @Test
    void testActualizarEstadoPedido_Success() throws Exception {
        Pedido pedidoActualizado = new Pedido();
        pedidoActualizado.setId("507f1f77bcf86cd799439011");
        pedidoActualizado.setEstado(Pedido.EstadoPedido.EN_PREPARACION);
        pedidoActualizado.setNumeroPedido("PED-001");
        pedidoActualizado.setClienteId(1L);
        pedidoActualizado.setMontoTotal(new BigDecimal("1500.00"));

        when(pedidoService.actualizarEstadoPedido("507f1f77bcf86cd799439011", Pedido.EstadoPedido.EN_PREPARACION))
                .thenReturn(pedidoActualizado);

        Map<String, Pedido.EstadoPedido> request = new HashMap<>();
        request.put("nuevoEstado", Pedido.EstadoPedido.EN_PREPARACION);

        mockMvc.perform(put("/api/pedidos/507f1f77bcf86cd799439011/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("507f1f77bcf86cd799439011")))
                .andExpect(jsonPath("$.estado", is("EN_PREPARACION")));

        verify(pedidoService, times(1))
                .actualizarEstadoPedido("507f1f77bcf86cd799439011", Pedido.EstadoPedido.EN_PREPARACION);
    }

    @Test
    void testActualizarEstadoPedido_NotFound() throws Exception {
        when(pedidoService.actualizarEstadoPedido("nonexistent", Pedido.EstadoPedido.EN_PREPARACION))
                .thenReturn(null);

        Map<String, Pedido.EstadoPedido> request = new HashMap<>();
        request.put("nuevoEstado", Pedido.EstadoPedido.EN_PREPARACION);

        mockMvc.perform(put("/api/pedidos/nonexistent/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(pedidoService, times(1))
                .actualizarEstadoPedido("nonexistent", Pedido.EstadoPedido.EN_PREPARACION);
    }

    @Test
    void testActualizarEstadoPedido_IllegalState() throws Exception {
        when(pedidoService.actualizarEstadoPedido("507f1f77bcf86cd799439011", Pedido.EstadoPedido.CANCELADO))
                .thenThrow(new IllegalStateException("No se puede cambiar al estado CANCELADO"));

        Map<String, Pedido.EstadoPedido> request = new HashMap<>();
        request.put("nuevoEstado", Pedido.EstadoPedido.CANCELADO);

        mockMvc.perform(put("/api/pedidos/507f1f77bcf86cd799439011/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(pedidoService, times(1))
                .actualizarEstadoPedido("507f1f77bcf86cd799439011", Pedido.EstadoPedido.CANCELADO);
    }

    @Test
    void testActualizarEstadoPedido_InternalError() throws Exception {
        when(pedidoService.actualizarEstadoPedido("507f1f77bcf86cd799439011", Pedido.EstadoPedido.ENTREGADO))
                .thenThrow(new RuntimeException("Error interno"));

        Map<String, Pedido.EstadoPedido> request = new HashMap<>();
        request.put("nuevoEstado", Pedido.EstadoPedido.ENTREGADO);

        mockMvc.perform(put("/api/pedidos/507f1f77bcf86cd799439011/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(pedidoService, times(1))
                .actualizarEstadoPedido("507f1f77bcf86cd799439011", Pedido.EstadoPedido.ENTREGADO);
    }
}
