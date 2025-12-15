package isi.dan.msclientes.servicios;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.dao.UsuarioRepository;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

  @Mock
  private ClienteRepository clienteRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private ObraRepository obraRepository;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private ClienteService clienteService;

  private Cliente clienteValido;
  private Cliente clienteExistente;
  private Usuario usuarioTest;
  private BigDecimal maximoDescubiertoDefault = BigDecimal.valueOf(5000);

  @BeforeEach
  void setUp() {
    // Configurar el valor por defecto del máximo descubierto
    ReflectionTestUtils.setField(clienteService, "maximoDescubiertoDefault", maximoDescubiertoDefault);

    clienteValido = new Cliente();
    clienteValido.setNombre("Juan");
    clienteValido.setCorreoElectronico("juan.perez@email.com");
    clienteValido.setCuit("20-12345678-9");
    clienteValido.setMaximoDescubierto(BigDecimal.valueOf(10000));

    clienteExistente = new Cliente();
    clienteExistente.setId(1);
    clienteExistente.setNombre("María");
    clienteExistente.setCorreoElectronico("maria.gonzalez@email.com");
    clienteExistente.setCuit("27-98765432-1");
    clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(15000));
    clienteExistente.setUsuarios(new ArrayList<>());

    usuarioTest = new Usuario();
    usuarioTest.setId(1);
    usuarioTest.setNombre("Test");
    usuarioTest.setApellido("Usuario");
    usuarioTest.setClientes(new ArrayList<>());
  }

  @Test
  void crearCliente_ConDatosValidos_DeberiaCrearCliente() {
    // Given
    when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

    // When
    Cliente resultado = clienteService.save(clienteValido);

    // Then
    assertThat(resultado).isNotNull();
    assertThat(resultado.getNombre()).isEqualTo("Juan");
    assertThat(resultado.getCorreoElectronico()).isEqualTo("juan.perez@email.com");
    verify(clienteRepository).save(clienteValido);
  }

  @Test
  void crearCliente_SinMaximoDescubierto_DeberiaAsignarValorPorDefecto() {
    // Given
    clienteValido.setMaximoDescubierto(null);
    when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
      Cliente c = invocation.getArgument(0);
      return c;
    });

    // When
    Cliente resultado = clienteService.save(clienteValido);

    // Then
    assertThat(resultado.getMaximoDescubierto()).isEqualTo(maximoDescubiertoDefault);
    verify(clienteRepository).save(clienteValido);
  }

  @Test
  void crearClienteConUsuario_DeberiaAsociarAlUsuario() {
    // Given
    when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);
    when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioTest);

    // When
    Cliente resultado = clienteService.save(clienteValido, 1);

    // Then
    assertThat(resultado).isNotNull();
    verify(clienteRepository).save(clienteValido);
    verify(usuarioRepository).findById(1);
    verify(usuarioRepository).save(usuarioTest);
  }

  @Test
  void findById_ConIdExistente_DeberiaRetornarCliente() {
    // Given
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));

    // When
    Optional<Cliente> resultado = clienteService.findById(1);

    // Then
    assertThat(resultado).isPresent();
    assertThat(resultado.get().getId()).isEqualTo(1);
    assertThat(resultado.get().getNombre()).isEqualTo("María");
    verify(clienteRepository).findById(1);
  }

  @Test
  void findById_ConIdInexistente_DeberiaRetornarVacio() {
    // Given
    when(clienteRepository.findById(999)).thenReturn(Optional.empty());

    // When
    Optional<Cliente> resultado = clienteService.findById(999);

    // Then
    assertThat(resultado).isEmpty();
    verify(clienteRepository).findById(999);
  }

  @Test
  void actualizar_ConDatosValidos_DeberiaActualizarCliente() {
    // Given
    Cliente clienteActualizado = new Cliente();
    clienteActualizado.setId(1);
    clienteActualizado.setNombre("Juan Carlos");
    clienteActualizado.setCorreoElectronico("juan.carlos@email.com");
    clienteActualizado.setCuit("20-12345678-9");
    clienteActualizado.setMaximoDescubierto(BigDecimal.valueOf(20000));

    when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

    // When
    Cliente resultado = clienteService.update(clienteActualizado);

    // Then
    assertThat(resultado).isNotNull();
    assertThat(resultado.getNombre()).isEqualTo("Juan Carlos");
    assertThat(resultado.getMaximoDescubierto()).isEqualTo(BigDecimal.valueOf(20000));
    verify(clienteRepository).save(any(Cliente.class));
  }

  @Test
  void eliminar_ConIdExistente_DeberiaEliminarCliente() {
    // Given
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(obraRepository.findByCliente(clienteExistente)).thenReturn(new ArrayList<>());
    doNothing().when(clienteRepository).deleteById(1);

    // When
    clienteService.deleteById(1);

    // Then
    verify(clienteRepository).findById(1);
    verify(obraRepository).findByCliente(clienteExistente);
    verify(clienteRepository).deleteById(1);
  }

  @Test
  void eliminar_ConObrasAsociadas_DeberiaLanzarExcepcion() {
    // Given
    Obra obra = new Obra();
    obra.setId(1);
    obra.setCliente(clienteExistente);
    
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(obraRepository.findByCliente(clienteExistente)).thenReturn(Arrays.asList(obra));

    // When & Then
    assertThatThrownBy(() -> clienteService.deleteById(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("tiene 1 obras asociadas");

    verify(clienteRepository).findById(1);
    verify(obraRepository).findByCliente(clienteExistente);
    verify(clienteRepository, never()).deleteById(any());
  }

  @Test
  void eliminar_DeberiaDesasociarUsuarios() {
    // Given
    usuarioTest.getClientes().add(clienteExistente);
    clienteExistente.getUsuarios().add(usuarioTest);
    
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(obraRepository.findByCliente(clienteExistente)).thenReturn(new ArrayList<>());
    when(usuarioRepository.save(usuarioTest)).thenReturn(usuarioTest);
    doNothing().when(clienteRepository).deleteById(1);

    // When
    clienteService.deleteById(1);

    // Then
    verify(usuarioRepository).save(usuarioTest);
    verify(clienteRepository).deleteById(1);
    assertThat(usuarioTest.getClientes()).doesNotContain(clienteExistente);
  }

  @Test
  void listarTodos_DeberiaRetornarListaDeClientes() {
    // Given
    List<Cliente> clientes = Arrays.asList(clienteExistente, clienteValido);
    when(clienteRepository.findAll()).thenReturn(clientes);

    // When
    List<Cliente> resultado = clienteService.findAll(null);

    // Then
    assertThat(resultado).hasSize(2);
    assertThat(resultado).contains(clienteExistente, clienteValido);
    verify(clienteRepository).findAll();
  }

  @Test
  void obtenerPorEmail_ConEmailExistente_DeberiaRetornarCliente() {
    // Given
    when(clienteRepository.findByCorreoElectronico("maria.gonzalez@email.com"))
        .thenReturn(Optional.of(clienteExistente));

    // When
    Optional<Cliente> resultado = clienteService.findByEmail("maria.gonzalez@email.com");

    // Then
    assertThat(resultado).isPresent();
    assertThat(resultado.get().getCorreoElectronico()).isEqualTo("maria.gonzalez@email.com");
    verify(clienteRepository).findByCorreoElectronico("maria.gonzalez@email.com");
  }

  @Test
  void obtenerPorEmail_ConEmailInexistente_DeberiaRetornarVacio() {
    // Given
    when(clienteRepository.findByCorreoElectronico("inexistente@email.com")).thenReturn(Optional.empty());

    // When
    Optional<Cliente> resultado = clienteService.findByEmail("inexistente@email.com");

    // Then
    assertThat(resultado).isEmpty();
    verify(clienteRepository).findByCorreoElectronico("inexistente@email.com");
  }

  // ========== Tests de Reglas de Negocio: Verificación de Saldo ==========

  @Test
  void verificarSaldoDisponible_ConSaldoSuficiente_DeberiaRetornarTrue() {
    // Given
    clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(10000));
    BigDecimal montoPedido = BigDecimal.valueOf(3000);
    BigDecimal montoCompromisoPendiente = BigDecimal.valueOf(2000);
    
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(restTemplate.getForObject(anyString(), eq(BigDecimal.class)))
        .thenReturn(montoCompromisoPendiente);

    // When
    boolean resultado = clienteService.verificarSaldoDisponible(1, montoPedido);

    // Then
    assertThat(resultado).isTrue();
    verify(clienteRepository).findById(1);
  }

  @Test
  void verificarSaldoDisponible_SinSaldoSuficiente_DeberiaRetornarFalse() {
    // Given
    clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(10000));
    BigDecimal montoPedido = BigDecimal.valueOf(8000);
    BigDecimal montoCompromisoPendiente = BigDecimal.valueOf(5000);
    
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(restTemplate.getForObject(anyString(), eq(BigDecimal.class)))
        .thenReturn(montoCompromisoPendiente);

    // When
    boolean resultado = clienteService.verificarSaldoDisponible(1, montoPedido);

    // Then
    assertThat(resultado).isFalse();
  }

  @Test
  void verificarSaldoDisponible_ConMontoExactoAlLimite_DeberiaRetornarTrue() {
    // Given
    clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(10000));
    BigDecimal montoPedido = BigDecimal.valueOf(4000);
    BigDecimal montoCompromisoPendiente = BigDecimal.valueOf(6000);
    
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(restTemplate.getForObject(anyString(), eq(BigDecimal.class)))
        .thenReturn(montoCompromisoPendiente);

    // When
    boolean resultado = clienteService.verificarSaldoDisponible(1, montoPedido);

    // Then
    assertThat(resultado).isTrue();
  }

  @Test
  void verificarSaldoDisponible_ConClienteInexistente_DeberiaRetornarFalse() {
    // Given
    when(clienteRepository.findById(999)).thenReturn(Optional.empty());

    // When
    boolean resultado = clienteService.verificarSaldoDisponible(999, BigDecimal.valueOf(1000));

    // Then
    assertThat(resultado).isFalse();
    verify(clienteRepository).findById(999);
  }

  @Test
  void verificarSaldoDisponible_ConErrorEnServicioPedidos_DeberiaRetornarFalse() {
    // Given - El servicio retorna 0 cuando hay error, lo que puede ser peligroso
    // pero es el comportamiento actual del servicio
    clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(100));
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(restTemplate.getForObject(anyString(), eq(BigDecimal.class)))
        .thenThrow(new RuntimeException("Servicio no disponible"));

    // When - El pedido de 1000 es mayor que el límite de 100
    boolean resultado = clienteService.verificarSaldoDisponible(1, BigDecimal.valueOf(1000));

    // Then - Debería retornar false porque el pedido excede el límite
    assertThat(resultado).isFalse();
  }

  @Test
  void findByUsuarioId_ConUsuarioConClientes_DeberiaRetornarClientes() {
    // Given
    usuarioTest.getClientes().add(clienteExistente);
    when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));

    // When
    List<Cliente> resultado = clienteService.findByUsuarioId(1, null);

    // Then
    assertThat(resultado).hasSize(1);
    assertThat(resultado).contains(clienteExistente);
  }

  @Test
  void findByUsuarioId_ConUsuarioSinClientes_DeberiaRetornarListaVacia() {
    // Given
    when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));

    // When
    List<Cliente> resultado = clienteService.findByUsuarioId(1, null);

    // Then
    assertThat(resultado).isEmpty();
  }

  @Test
  void findByUsuarioId_ConFiltro_DeberiaAplicarFiltro() {
    // Given
    Cliente cliente2 = new Cliente();
    cliente2.setNombre("Pedro");
    cliente2.setCorreoElectronico("pedro@test.com");
    cliente2.setCuit("20-11111111-1");
    
    usuarioTest.getClientes().add(clienteExistente);
    usuarioTest.getClientes().add(cliente2);
    when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));

    // When
    List<Cliente> resultado = clienteService.findByUsuarioId(1, "María");

    // Then
    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).getNombre()).contains("María");
  }

}
