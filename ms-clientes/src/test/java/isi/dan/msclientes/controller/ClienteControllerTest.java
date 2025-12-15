package isi.dan.msclientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.servicios.ClienteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Test Cliente");
        cliente.setCorreoElectronico("test@cliente.com");
        cliente.setCuit("12998887776");
    }

    @Test
    void testGetAll() throws Exception {
        Mockito.when(clienteService.findAll(null)).thenReturn(Collections.singletonList(cliente));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nombre").value("Test Cliente"));
    }

    @Test
    void testGetAllWithSearchTerm() throws Exception {
        Mockito.when(clienteService.findAll("Test")).thenReturn(Collections.singletonList(cliente));

        mockMvc.perform(get("/api/clientes")
                .param("searchTerm", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nombre").value("Test Cliente"));
    }

    @Test
    void testGetAllByUsuarioId() throws Exception {
        Mockito.when(clienteService.findByUsuarioId(1, null)).thenReturn(Collections.singletonList(cliente));

        mockMvc.perform(get("/api/clientes")
                .param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nombre").value("Test Cliente"));
    }

    @Test
    void testGetEcho() throws Exception {
        mockMvc.perform(get("/api/clientes/echo"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(" - ")));
    }

    @Test
    void testGetById() throws Exception {
        Mockito.when(clienteService.findById(1)).thenReturn(Optional.of(cliente));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Test Cliente"))
                .andExpect(jsonPath("$.cuit").value("12998887776"));
    }
    @Test
    void testGetById_NotFound() throws Exception {
        Mockito.when(clienteService.findById(2)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clientes/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        Mockito.when(clienteService.save(Mockito.any(Cliente.class), Mockito.eq(1))).thenReturn(cliente);

        mockMvc.perform(post("/api/clientes")
                .param("usuarioId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test Cliente"));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito.when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        Mockito.when(clienteService.update(Mockito.any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(put("/api/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test Cliente"));
    }

    @Test
    void testDelete() throws Exception {
        Mockito.when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        Mockito.doNothing().when(clienteService).deleteById(1);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testVerificarSaldoCliente_ConSaldo() throws Exception {
        BigDecimal monto = BigDecimal.valueOf(5000);
        Mockito.when(clienteService.verificarSaldoDisponible(1, monto)).thenReturn(true);

        mockMvc.perform(get("/api/clientes/1/saldo/5000"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testVerificarSaldoCliente_SinSaldo() throws Exception {
        BigDecimal monto = BigDecimal.valueOf(15000);
        Mockito.when(clienteService.verificarSaldoDisponible(1, monto)).thenReturn(false);

        mockMvc.perform(get("/api/clientes/1/saldo/15000"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testVerificarSaldoCliente_Error() throws Exception {
        BigDecimal monto = BigDecimal.valueOf(5000);
        Mockito.when(clienteService.verificarSaldoDisponible(1, monto))
                .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/clientes/1/saldo/5000"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("false"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

