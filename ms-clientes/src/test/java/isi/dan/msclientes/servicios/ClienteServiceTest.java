package isi.dan.msclientes.servicios;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.EmailDuplicadoException;
import isi.dan.msclientes.exception.CuitDuplicadoException;
import isi.dan.msclientes.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

  @InjectMocks
  private ClienteService clienteService;

  private Cliente clienteValido;
  private Cliente clienteExistente;

  @BeforeEach
  void setUp() {
    clienteValido = new Cliente();
    clienteValido.setNombre("Juan Pérez");
    clienteValido.setCorreoElectronico("juan.perez@email.com");
    clienteValido.setCuit("20-12345678-9");
    clienteValido.setMaximoDescubierto(BigDecimal.valueOf(10000));

    clienteExistente = new Cliente();
    clienteExistente.setId(1);
    clienteExistente.setNombre("María González");
    clienteExistente.setCorreoElectronico("maria.gonzalez@email.com");
    clienteExistente.setCuit("27-98765432-1");
    clienteExistente.setMaximoDescubierto(BigDecimal.valueOf(15000));
  }

  @Test
  void crearCliente_ConDatosValidos_DeberiaCrearCliente() {
    // Given
    when(clienteRepository.findByCorreoElectronico(clienteValido.getCorreoElectronico())).thenReturn(Optional.empty());
    when(clienteRepository.findByCuit(clienteValido.getCuit())).thenReturn(Optional.empty());
    when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteValido);

    // When
    Cliente resultado = clienteService.save(clienteValido);

    // Then
    assertThat(resultado).isNotNull();
    assertThat(resultado.getNombre()).isEqualTo("Juan");
    assertThat(resultado.getCorreoElectronico()).isEqualTo("juan.perez@email.com");
    verify(clienteRepository).findByCorreoElectronico(clienteValido.getCorreoElectronico());
    verify(clienteRepository).findByCuit(clienteValido.getCuit());
    verify(clienteRepository).save(clienteValido);
  }

  @Test
  void crearCliente_ConEmailDuplicado_DeberiaLanzarExcepcion() {
    // Given
    when(clienteRepository.findByCorreoElectronico(clienteValido.getCorreoElectronico()))
        .thenReturn(Optional.of(clienteExistente));

    // When & Then
    assertThatThrownBy(() -> clienteService.save(clienteValido))
        .isInstanceOf(EmailDuplicadoException.class)
        .hasMessageContaining("El email ya está registrado");

    verify(clienteRepository).findByCorreoElectronico(clienteValido.getCorreoElectronico());
    verify(clienteRepository, never()).save(any());
  }

  @Test
  void crearCliente_ConCuitDuplicado_DeberiaLanzarExcepcion() {
    // Given
    when(clienteRepository.findByCorreoElectronico(clienteValido.getCorreoElectronico())).thenReturn(Optional.empty());
    when(clienteRepository.findByCuit(clienteValido.getCuit())).thenReturn(Optional.of(clienteExistente));

    // When & Then
    assertThatThrownBy(() -> clienteService.save(clienteValido))
        .isInstanceOf(CuitDuplicadoException.class)
        .hasMessageContaining("El CUIT ya está registrado");

    verify(clienteRepository).findByCorreoElectronico(clienteValido.getCorreoElectronico());
    verify(clienteRepository).findByCuit(clienteValido.getCuit());
    verify(clienteRepository, never()).save(any());
  }

  @Test
  void crearCliente_ConNombreNulo_DeberiaLanzarExcepcion() {
    // Given
    clienteValido.setNombre(null);

    // When & Then
    assertThatThrownBy(() -> clienteService.save(clienteValido))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El nombre es obligatorio");

    verify(clienteRepository, never()).save(any());
  }

  @Test
  void crearCliente_ConEmailInvalido_DeberiaLanzarExcepcion() {
    // Given
    clienteValido.setCorreoElectronico("email-invalido");

    // When & Then
    assertThatThrownBy(() -> clienteService.save(clienteValido))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El formato del email es inválido");

    verify(clienteRepository, never()).save(any());
  }

  @Test
  void crearCliente_ConCuitInvalido_DeberiaLanzarExcepcion() {
    // Given
    clienteValido.setCuit("123456789"); // CUIT inválido

    // When & Then
    assertThatThrownBy(() -> clienteService.save(clienteValido))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El formato del CUIT es inválido");

    verify(clienteRepository, never()).save(any());
  }

  @Test
  void crearCliente_ConMaximoDescubiertoNegativo_DeberiaLanzarExcepcion() {
    // Given
    clienteValido.setMaximoDescubierto(BigDecimal.valueOf(-1000));

    // When & Then
    assertThatThrownBy(() -> clienteService.save(clienteValido))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El máximo descubierto no puede ser negativo");

    verify(clienteRepository, never()).save(any());
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
  void findById_ConIdInexistente_DeberiaLanzarExcepcion() {
    // Given
    when(clienteRepository.findById(999)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> clienteService.findById(999))
        .isInstanceOf(ClienteNotFoundException.class)
        .hasMessageContaining("Cliente no encontrado con ID: 999");

    verify(clienteRepository).findById(999);
  }

  @Test
  void actualizar_ConDatosValidos_DeberiaActualizarCliente() {
    // Given
    Cliente clienteActualizado = new Cliente();
    clienteActualizado.setId(1);
    clienteActualizado.setNombre("Juan Carlos Pérez López");
    clienteActualizado.setCorreoElectronico("juan.carlos@email.com");
    clienteActualizado.setCuit("20-12345678-9");
    clienteActualizado.setMaximoDescubierto(BigDecimal.valueOf(20000));

    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    when(clienteRepository.findByCorreoElectronico("juan.carlos@email.com")).thenReturn(Optional.empty());
    when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

    // When
    Cliente resultado = clienteService.update(clienteActualizado);

    // Then
    assertThat(resultado).isNotNull();
    assertThat(resultado.getNombre()).isEqualTo("Juan Carlos");
    assertThat(resultado.getMaximoDescubierto()).isEqualTo(BigDecimal.valueOf(20000));
    verify(clienteRepository).findById(1);
    verify(clienteRepository).save(any(Cliente.class));
  }

  @Test
  void eliminar_ConIdExistente_DeberiaEliminarCliente() {
    // Given
    when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteExistente));
    doNothing().when(clienteRepository).deleteById(1);

    // When
    clienteService.deleteById(1);

    // Then
    verify(clienteRepository).findById(1);
    verify(clienteRepository).deleteById(1);
  }

  @Test
  void eliminar_ConIdInexistente_DeberiaLanzarExcepcion() {
    // Given
    when(clienteRepository.findById(999)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> clienteService.deleteById(999))
        .isInstanceOf(ClienteNotFoundException.class)
        .hasMessageContaining("Cliente no encontrado con ID: 999");

    verify(clienteRepository).findById(999);
    verify(clienteRepository, never()).deleteById(any());
  }

  @Test
  void listarTodos_DeberiaRetornarListaDeClientes() {
    // Given
    List<Cliente> clientes = Arrays.asList(clienteExistente, clienteValido);
    when(clienteRepository.findAll()).thenReturn(clientes);

    // When
    List<Cliente> resultado = clienteService.findAll();

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
  void obtenerPorEmail_ConEmailInexistente_DeberiaLanzarExcepcion() {
    // Given
    when(clienteRepository.findByCorreoElectronico("inexistente@email.com")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> clienteService.findByEmail("inexistente@email.com"))
        .isInstanceOf(ClienteNotFoundException.class)
        .hasMessageContaining("Cliente no encontrado con email: inexistente@email.com");

    verify(clienteRepository).findByCorreoElectronico("inexistente@email.com");
  }

}
