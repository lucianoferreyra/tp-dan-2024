// package isi.dan.msclientes.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import isi.dan.msclientes.model.Cliente;
// import isi.dan.msclientes.model.Usuario;
// import isi.dan.msclientes.servicios.UsuarioService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import java.math.BigDecimal;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(UsuarioController.class)
// public class UsuarioControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private UsuarioService usuarioService;

//     private Usuario usuario;
//     private Cliente cliente;

//     @BeforeEach
//     void setUp() {
//         cliente = new Cliente();
//         cliente.setId(1);
//         cliente.setNombre("Empresa Test");
//         cliente.setCorreoElectronico("empresa@test.com");
//         cliente.setCuit("20-12345678-9");
//         cliente.setMaximoDescubierto(BigDecimal.valueOf(10000));

//         usuario = new Usuario();
//         usuario.setId(1);
//         usuario.setNombre("Juan");
//         usuario.setApellido("Pérez");
//         usuario.setDni("12345678");
//         usuario.setCorreoElectronico("juan.perez@test.com");
//         usuario.setCliente(cliente);
//     }

//     @Test
//     void testGetAll() throws Exception {
//         List<Usuario> usuarios = Arrays.asList(usuario);
//         Mockito.when(usuarioService.findAll()).thenReturn(usuarios);

//         mockMvc.perform(get("/api/usuarios"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$[0].nombre").value("Juan"))
//                 .andExpect(jsonPath("$[0].apellido").value("Pérez"));
//     }

//     @Test
//     void testGetByClienteId() throws Exception {
//         List<Usuario> usuarios = Arrays.asList(usuario);
//         Mockito.when(usuarioService.findByClienteId(1)).thenReturn(usuarios);

//         mockMvc.perform(get("/api/usuarios/cliente/1"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$[0].nombre").value("Juan"));
//     }

//     @Test
//     void testGetById() throws Exception {
//         Mockito.when(usuarioService.findById(1)).thenReturn(Optional.of(usuario));

//         mockMvc.perform(get("/api/usuarios/1"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.nombre").value("Juan"))
//                 .andExpect(jsonPath("$.dni").value("12345678"));
//     }

//     @Test
//     void testGetById_NotFound() throws Exception {
//         Mockito.when(usuarioService.findById(999)).thenReturn(Optional.empty());

//         mockMvc.perform(get("/api/usuarios/999"))
//                 .andExpect(status().isNotFound());
//     }

//     @Test
//     void testGetByDni() throws Exception {
//         Mockito.when(usuarioService.findByDni("12345678")).thenReturn(Optional.of(usuario));

//         mockMvc.perform(get("/api/usuarios/buscar/dni/12345678"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.nombre").value("Juan"))
//                 .andExpect(jsonPath("$.dni").value("12345678"));
//     }

//     @Test
//     void testGetByEmail() throws Exception {
//         Mockito.when(usuarioService.findByCorreoElectronico("juan.perez@test.com"))
//                 .thenReturn(Optional.of(usuario));

//         mockMvc.perform(get("/api/usuarios/buscar/email/juan.perez@test.com"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(jsonPath("$.nombre").value("Juan"))
//                 .andExpect(jsonPath("$.correoElectronico").value("juan.perez@test.com"));
//     }

//     @Test
//     void testCreate() throws Exception {
//         Mockito.when(usuarioService.save(Mockito.any(Usuario.class))).thenReturn(usuario);

//         mockMvc.perform(post("/api/usuarios")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(usuario)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.nombre").value("Juan"))
//                 .andExpect(jsonPath("$.apellido").value("Pérez"));
//     }

//     @Test
//     void testUpdate() throws Exception {
//         Mockito.when(usuarioService.update(Mockito.eq(1), Mockito.any(Usuario.class))).thenReturn(usuario);

//         mockMvc.perform(put("/api/usuarios/1")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(usuario)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.nombre").value("Juan"));
//     }

//     @Test
//     void testDelete() throws Exception {
//         Mockito.doNothing().when(usuarioService).deleteById(1);

//         mockMvc.perform(delete("/api/usuarios/1"))
//                 .andExpect(status().isNoContent());
//     }

//     private static String asJsonString(final Object obj) {
//         try {
//             return new ObjectMapper().writeValueAsString(obj);
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//     }
// }
