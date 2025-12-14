// package isi.dan.msclientes.servicios;

// import isi.dan.msclientes.dao.ClienteRepository;
// import isi.dan.msclientes.dao.UsuarioRepository;
// import isi.dan.msclientes.exception.ClienteNotFoundException;
// import isi.dan.msclientes.exception.DniDuplicadoException;
// import isi.dan.msclientes.exception.EmailUsuarioDuplicadoException;
// import isi.dan.msclientes.exception.UsuarioNotFoundException;
// import isi.dan.msclientes.model.Cliente;
// import isi.dan.msclientes.model.Usuario;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.math.BigDecimal;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class UsuarioServiceTest {

//     @Mock
//     private UsuarioRepository usuarioRepository;

//     @Mock
//     private ClienteRepository clienteRepository;

//     @InjectMocks
//     private UsuarioService usuarioService;

//     private Usuario usuarioValido;
//     private Cliente clienteExistente;
//     private Usuario usuarioExistente;

//     @BeforeEach
//     void setUp() {
//         clienteExistente = new Cliente();
//         clienteExistente.setId(1);
//         clienteExistente.setNombre("Empresa Test");
//         clienteExistente.setCorreoElectronico("empresa@test.com");
//         clienteExistente.setCuit("20-12345678-9");
//         clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(10000));

//         usuarioValido = new Usuario();
//         usuarioValido.setNombre("Juan");
//         usuarioValido.setApellido("Pérez");
//         usuarioValido.setDni("12345678");
//         usuarioValido.setCorreoElectronico("juan.perez@test.com");
//         usuarioValido.setCliente(clienteExistente);

//         usuarioExistente = new Usuario();
//         usuarioExistente.setId(1);
//         usuarioExistente.setNombre("María");
//         usuarioExistente.setApellido("González");
//         usuarioExistente.setDni("87654321");
//         usuarioExistente.setCorreoElectronico("maria.gonzalez@test.com");
//         usuarioExistente.setCliente(clienteExistente);
//     }

//     @Test
//     void crearUsuario_ConDatosValidos_DeberiaCrearUsuario() throws Exception {
//         // Given
//         when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
//         when(usuarioRepository.existsByDni(usuarioValido.getDni())).thenReturn(false);
//         when(usuarioRepository.existsByCorreoElectronico(usuarioValido.getCorreoElectronico())).thenReturn(false);
//         when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioValido);

//         // When
//         Usuario resultado = usuarioService.save(usuarioValido);

//         // Then
//         assertThat(resultado).isNotNull();
//         assertThat(resultado.getNombre()).isEqualTo("Juan");
//         assertThat(resultado.getApellido()).isEqualTo("Pérez");
//         assertThat(resultado.getDni()).isEqualTo("12345678");
//         verify(clienteRepository).findById(1);
//         verify(usuarioRepository).existsByDni(usuarioValido.getDni());
//         verify(usuarioRepository).existsByCorreoElectronico(usuarioValido.getCorreoElectronico());
//         verify(usuarioRepository).save(usuarioValido);
//     }

//     @Test
//     void crearUsuario_SinCliente_DeberiaLanzarExcepcion() {
//         // Given
//         usuarioValido.setCliente(null);

//         // When & Then
//         assertThatThrownBy(() -> usuarioService.save(usuarioValido))
//                 .isInstanceOf(ClienteNotFoundException.class)
//                 .hasMessageContaining("El cliente es obligatorio");

//         verify(usuarioRepository, never()).save(any());
//     }

//     @Test
//     void crearUsuario_ConClienteInexistente_DeberiaLanzarExcepcion() {
//         // Given
//         when(clienteRepository.findById(1)).thenReturn(Optional.empty());

//         // When & Then
//         assertThatThrownBy(() -> usuarioService.save(usuarioValido))
//                 .isInstanceOf(ClienteNotFoundException.class)
//                 .hasMessageContaining("Cliente no encontrado");

//         verify(clienteRepository).findById(1);
//         verify(usuarioRepository, never()).save(any());
//     }

//     @Test
//     void crearUsuario_ConDniDuplicado_DeberiaLanzarExcepcion() {
//         // Given
//         when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
//         when(usuarioRepository.existsByDni(usuarioValido.getDni())).thenReturn(true);

//         // When & Then
//         assertThatThrownBy(() -> usuarioService.save(usuarioValido))
//                 .isInstanceOf(DniDuplicadoException.class)
//                 .hasMessageContaining("Ya existe un usuario con el DNI");

//         verify(clienteRepository).findById(1);
//         verify(usuarioRepository).existsByDni(usuarioValido.getDni());
//         verify(usuarioRepository, never()).save(any());
//     }

//     @Test
//     void crearUsuario_ConEmailDuplicado_DeberiaLanzarExcepcion() {
//         // Given
//         when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
//         when(usuarioRepository.existsByDni(usuarioValido.getDni())).thenReturn(false);
//         when(usuarioRepository.existsByCorreoElectronico(usuarioValido.getCorreoElectronico())).thenReturn(true);

//         // When & Then
//         assertThatThrownBy(() -> usuarioService.save(usuarioValido))
//                 .isInstanceOf(EmailUsuarioDuplicadoException.class)
//                 .hasMessageContaining("Ya existe un usuario con el email");

//         verify(clienteRepository).findById(1);
//         verify(usuarioRepository).existsByDni(usuarioValido.getDni());
//         verify(usuarioRepository).existsByCorreoElectronico(usuarioValido.getCorreoElectronico());
//         verify(usuarioRepository, never()).save(any());
//     }

//     @Test
//     void actualizarUsuario_ConDatosValidos_DeberiaActualizarUsuario() throws Exception {
//         // Given
//         Usuario usuarioActualizado = new Usuario();
//         usuarioActualizado.setNombre("Juan Carlos");
//         usuarioActualizado.setApellido("Pérez López");
//         usuarioActualizado.setDni("12345678");
//         usuarioActualizado.setCorreoElectronico("juan.perez@test.com");

//         when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistente));
//         when(usuarioRepository.existsByDni(usuarioActualizado.getDni())).thenReturn(false);
//         when(usuarioRepository.existsByCorreoElectronico(usuarioActualizado.getCorreoElectronico())).thenReturn(false);
//         when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

//         // When
//         Usuario resultado = usuarioService.update(1, usuarioActualizado);

//         // Then
//         assertThat(resultado).isNotNull();
//         verify(usuarioRepository).findById(1);
//         verify(usuarioRepository).save(usuarioExistente);
//     }

//     @Test
//     void actualizarUsuario_Inexistente_DeberiaLanzarExcepcion() {
//         // Given
//         when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

//         // When & Then
//         assertThatThrownBy(() -> usuarioService.update(999, usuarioValido))
//                 .isInstanceOf(UsuarioNotFoundException.class)
//                 .hasMessageContaining("Usuario no encontrado");

//         verify(usuarioRepository).findById(999);
//         verify(usuarioRepository, never()).save(any());
//     }

//     @Test
//     void eliminarUsuario_Existente_DeberiaEliminar() throws Exception {
//         // Given
//         when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistente));
//         doNothing().when(usuarioRepository).deleteById(1);

//         // When
//         usuarioService.deleteById(1);

//         // Then
//         verify(usuarioRepository).findById(1);
//         verify(usuarioRepository).deleteById(1);
//     }

//     @Test
//     void eliminarUsuario_Inexistente_DeberiaLanzarExcepcion() {
//         // Given
//         when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

//         // When & Then
//         assertThatThrownBy(() -> usuarioService.deleteById(999))
//                 .isInstanceOf(UsuarioNotFoundException.class)
//                 .hasMessageContaining("Usuario no encontrado");

//         verify(usuarioRepository).findById(999);
//         verify(usuarioRepository, never()).deleteById(any());
//     }

//     @Test
//     void buscarUsuariosPorCliente_DeberiaRetornarLista() {
//         // Given
//         List<Usuario> usuarios = Arrays.asList(usuarioValido, usuarioExistente);
//         when(usuarioRepository.findByClienteId(1)).thenReturn(usuarios);

//         // When
//         List<Usuario> resultado = usuarioService.findByClienteId(1);

//         // Then
//         assertThat(resultado).hasSize(2);
//         assertThat(resultado).contains(usuarioValido, usuarioExistente);
//         verify(usuarioRepository).findByClienteId(1);
//     }

//     @Test
//     void buscarUsuarioPorDni_DeberiaRetornarUsuario() {
//         // Given
//         when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuarioValido));

//         // When
//         Optional<Usuario> resultado = usuarioService.findByDni("12345678");

//         // Then
//         assertThat(resultado).isPresent();
//         assertThat(resultado.get().getDni()).isEqualTo("12345678");
//         verify(usuarioRepository).findByDni("12345678");
//     }

//     @Test
//     void buscarUsuarioPorEmail_DeberiaRetornarUsuario() {
//         // Given
//         when(usuarioRepository.findByCorreoElectronico("juan.perez@test.com")).thenReturn(Optional.of(usuarioValido));

//         // When
//         Optional<Usuario> resultado = usuarioService.findByCorreoElectronico("juan.perez@test.com");

//         // Then
//         assertThat(resultado).isPresent();
//         assertThat(resultado.get().getCorreoElectronico()).isEqualTo("juan.perez@test.com");
//         verify(usuarioRepository).findByCorreoElectronico("juan.perez@test.com");
//     }
// }
