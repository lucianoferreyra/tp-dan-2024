package isi.dan.msclientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.msclientes.enums.EstadoObra;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ObraService;

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

@WebMvcTest(ObraController.class)
public class ObraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObraService obraService;

    private Obra obra;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Test");
        
        obra = new Obra();
        obra.setId(1);
        obra.setDireccion("Direccion Test Obra");
        obra.setPresupuesto(BigDecimal.valueOf(100));
        obra.setEstado(EstadoObra.PENDIENTE);
        obra.setCliente(cliente);
    }

    @Test
    void testGetAll() throws Exception {
        Mockito.when(obraService.findAll()).thenReturn(Collections.singletonList(obra));

        mockMvc.perform(get("/api/obras"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].direccion").value("Direccion Test Obra"));
    }

    @Test
    void testGetAllByUsuarioId() throws Exception {
        Mockito.when(obraService.findByUsuarioId(1)).thenReturn(Collections.singletonList(obra));

        mockMvc.perform(get("/api/obras")
                .param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].direccion").value("Direccion Test Obra"));
    }

    @Test
    void testGetById() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));

        mockMvc.perform(get("/api/obras/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.direccion").value("Direccion Test Obra"));
    }

    @Test
    void testCreate() throws Exception {
        Mockito.when(obraService.save(Mockito.any(Obra.class))).thenReturn(obra);

        mockMvc.perform(post("/api/obras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(obra)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Direccion Test Obra"));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.when(obraService.update(Mockito.any(Obra.class))).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(obra)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Direccion Test Obra"));
    }

    @Test
    void testDelete() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.doNothing().when(obraService).deleteById(1);

        mockMvc.perform(delete("/api/obras/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testFinalizarObra() throws Exception {
        obra.setEstado(EstadoObra.FINALIZADA);
        Mockito.when(obraService.finalizarObra(1)).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Obra finalizada exitosamente"))
                .andExpect(jsonPath("$.obra.estado").value("FINALIZADA"));
    }

    @Test
    void testFinalizarObra_Error() throws Exception {
        Mockito.when(obraService.finalizarObra(1))
                .thenThrow(new RuntimeException("No se puede finalizar la obra"));

        mockMvc.perform(put("/api/obras/1/finalizar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede finalizar la obra"));
    }

    @Test
    void testPasarAPendiente() throws Exception {
        obra.setEstado(EstadoObra.PENDIENTE);
        Mockito.when(obraService.pasarAPendiente(1)).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1/pendiente"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Obra pasada a estado pendiente exitosamente"))
                .andExpect(jsonPath("$.obra.estado").value("PENDIENTE"));
    }

    @Test
    void testPasarAPendiente_Error() throws Exception {
        Mockito.when(obraService.pasarAPendiente(1))
                .thenThrow(new RuntimeException("No se puede pasar a pendiente"));

        mockMvc.perform(put("/api/obras/1/pendiente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede pasar a pendiente"));
    }

    @Test
    void testPasarAHabilitada() throws Exception {
        obra.setEstado(EstadoObra.HABILITADA);
        Mockito.when(obraService.pasarAHabilitada(1)).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1/habilitar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Obra habilitada exitosamente"))
                .andExpect(jsonPath("$.obra.estado").value("HABILITADA"));
    }

    @Test
    void testPasarAHabilitada_Error() throws Exception {
        Mockito.when(obraService.pasarAHabilitada(1))
                .thenThrow(new RuntimeException("No se puede habilitar la obra"));

        mockMvc.perform(put("/api/obras/1/habilitar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede habilitar la obra"));
    }

    @Test
    void testAsignarCliente() throws Exception {
        obra.setEstado(EstadoObra.HABILITADA);
        Mockito.when(obraService.asignarCliente(1, 1)).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1/asignar-cliente/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Cliente asignado exitosamente. Estado de la obra: HABILITADA"))
                .andExpect(jsonPath("$.obra.estado").value("HABILITADA"))
                .andExpect(jsonPath("$.estado").value("HABILITADA"));
    }

    @Test
    void testAsignarCliente_Error() throws Exception {
        Mockito.when(obraService.asignarCliente(1, 1))
                .thenThrow(new RuntimeException("Cliente no encontrado"));

        mockMvc.perform(put("/api/obras/1/asignar-cliente/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cliente no encontrado"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

