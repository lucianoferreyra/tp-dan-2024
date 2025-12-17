package isi.dan.ms_productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.servicio.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Cementos");
        categoria.setId(1L);
    }

    @Test
    void testGetAllCategorias() throws Exception {
        Categoria categoria2 = new Categoria("Placas");
        categoria2.setId(2L);

        List<Categoria> categorias = Arrays.asList(categoria, categoria2);

        when(categoriaService.getAllCategorias()).thenReturn(categorias);

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nombre", is("Cementos")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].nombre", is("Placas")));

        verify(categoriaService, times(1)).getAllCategorias();
    }

    @Test
    void testGetCategoriaById_Success() throws Exception {
        when(categoriaService.getCategoriaById(1L)).thenReturn(categoria);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Cementos")));

        verify(categoriaService, times(1)).getCategoriaById(1L);
    }

    @Test
    void testGetCategoriaById_NotFound() throws Exception {
        when(categoriaService.getCategoriaById(999L))
                .thenThrow(new CategoriaNotFoundException(999L));

        mockMvc.perform(get("/api/categorias/999"))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).getCategoriaById(999L);
    }

    @Test
    void testGetCategoriaByNombre_Success() throws Exception {
        when(categoriaService.getCategoriaByNombre("Cementos")).thenReturn(categoria);

        mockMvc.perform(get("/api/categorias/nombre/Cementos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Cementos")));

        verify(categoriaService, times(1)).getCategoriaByNombre("Cementos");
    }

    @Test
    void testGetCategoriaByNombre_NotFound() throws Exception {
        when(categoriaService.getCategoriaByNombre("NoExiste"))
                .thenThrow(new CategoriaNotFoundException("NoExiste"));

        mockMvc.perform(get("/api/categorias/nombre/NoExiste"))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).getCategoriaByNombre("NoExiste");
    }

    @Test
    void testCreateCategoria_Success() throws Exception {
        Categoria newCategoria = new Categoria("Perfiles");

        Categoria savedCategoria = new Categoria("Perfiles");
        savedCategoria.setId(3L);

        when(categoriaService.existsByNombre("Perfiles")).thenReturn(false);
        when(categoriaService.saveCategoria(any(Categoria.class))).thenReturn(savedCategoria);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.nombre", is("Perfiles")));

        verify(categoriaService, times(1)).existsByNombre("Perfiles");
        verify(categoriaService, times(1)).saveCategoria(any(Categoria.class));
    }

    @Test
    void testCreateCategoria_Conflict() throws Exception {
        Categoria newCategoria = new Categoria("Cementos");

        when(categoriaService.existsByNombre("Cementos")).thenReturn(true);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoria)))
                .andExpect(status().isConflict());

        verify(categoriaService, times(1)).existsByNombre("Cementos");
        verify(categoriaService, never()).saveCategoria(any(Categoria.class));
    }

    @Test
    void testUpdateCategoria_Success() throws Exception {
        Categoria updateCategoria = new Categoria("Cementos Especiales");

        Categoria updatedCategoria = new Categoria("Cementos Especiales");
        updatedCategoria.setId(1L);

        when(categoriaService.existsByNombre("Cementos Especiales")).thenReturn(false);
        when(categoriaService.updateCategoria(eq(1L), any(Categoria.class)))
                .thenReturn(updatedCategoria);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCategoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Cementos Especiales")));

        verify(categoriaService, times(1)).existsByNombre("Cementos Especiales");
        verify(categoriaService, times(1)).updateCategoria(eq(1L), any(Categoria.class));
    }

    @Test
    void testUpdateCategoria_SameNameSameId() throws Exception {
        Categoria updateCategoria = new Categoria("Cementos");

        Categoria updatedCategoria = new Categoria("Cementos");
        updatedCategoria.setId(1L);

        when(categoriaService.existsByNombre("Cementos")).thenReturn(true);
        when(categoriaService.getCategoriaByNombre("Cementos")).thenReturn(categoria);
        when(categoriaService.updateCategoria(eq(1L), any(Categoria.class)))
                .thenReturn(updatedCategoria);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCategoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Cementos")));

        verify(categoriaService, times(1)).existsByNombre("Cementos");
        verify(categoriaService, times(1)).getCategoriaByNombre("Cementos");
        verify(categoriaService, times(1)).updateCategoria(eq(1L), any(Categoria.class));
    }

    @Test
    void testUpdateCategoria_ConflictDifferentId() throws Exception {
        Categoria updateCategoria = new Categoria("Placas");

        Categoria existingCategoria = new Categoria("Placas");
        existingCategoria.setId(2L);

        when(categoriaService.existsByNombre("Placas")).thenReturn(true);
        when(categoriaService.getCategoriaByNombre("Placas")).thenReturn(existingCategoria);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCategoria)))
                .andExpect(status().isConflict());

        verify(categoriaService, times(1)).existsByNombre("Placas");
        verify(categoriaService, times(1)).getCategoriaByNombre("Placas");
        verify(categoriaService, never()).updateCategoria(anyLong(), any(Categoria.class));
    }

    @Test
    void testUpdateCategoria_NotFound() throws Exception {
        Categoria updateCategoria = new Categoria("Nueva Categoría");

        when(categoriaService.existsByNombre("Nueva Categoría")).thenReturn(false);
        when(categoriaService.updateCategoria(eq(999L), any(Categoria.class)))
                .thenThrow(new CategoriaNotFoundException(999L));

        mockMvc.perform(put("/api/categorias/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCategoria)))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).existsByNombre("Nueva Categoría");
        verify(categoriaService, times(1)).updateCategoria(eq(999L), any(Categoria.class));
    }

    @Test
    void testDeleteCategoria_Success() throws Exception {
        doNothing().when(categoriaService).deleteCategoria(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).deleteCategoria(1L);
    }

    @Test
    void testDeleteCategoria_NotFound() throws Exception {
        doThrow(new CategoriaNotFoundException(999L))
                .when(categoriaService).deleteCategoria(999L);

        mockMvc.perform(delete("/api/categorias/999"))
                .andExpect(status().isNotFound());

        verify(categoriaService, times(1)).deleteCategoria(999L);
    }
}
