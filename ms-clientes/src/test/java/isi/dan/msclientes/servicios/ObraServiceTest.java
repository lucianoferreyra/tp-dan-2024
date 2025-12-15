package isi.dan.msclientes.servicios;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.dao.UsuarioRepository;
import isi.dan.msclientes.enums.EstadoObra;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ObraServiceTest {

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ObraService obraService;

    private Obra obraHabilitada;
    private Obra obraPendiente;
    private Obra obraFinalizada;
    private Cliente clienteConLimite;
    private Cliente clienteSinLimite;
    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        // Cliente con límite de 2 obras
        clienteConLimite = new Cliente();
        clienteConLimite.setId(1);
        clienteConLimite.setNombre("Cliente con Límite");
        clienteConLimite.setMaximoCantidadObrasEnEjecucion(2);

        // Cliente sin límite
        clienteSinLimite = new Cliente();
        clienteSinLimite.setId(2);
        clienteSinLimite.setNombre("Cliente sin Límite");
        clienteSinLimite.setMaximoCantidadObrasEnEjecucion(null);

        // Obra habilitada
        obraHabilitada = new Obra();
        obraHabilitada.setId(1);
        obraHabilitada.setDireccion("Dirección 1");
        obraHabilitada.setPresupuesto(BigDecimal.valueOf(10000));
        obraHabilitada.setEstado(EstadoObra.HABILITADA);
        obraHabilitada.setCliente(clienteConLimite);

        // Obra pendiente
        obraPendiente = new Obra();
        obraPendiente.setId(2);
        obraPendiente.setDireccion("Dirección 2");
        obraPendiente.setPresupuesto(BigDecimal.valueOf(15000));
        obraPendiente.setEstado(EstadoObra.PENDIENTE);
        obraPendiente.setCliente(clienteConLimite);

        // Obra finalizada
        obraFinalizada = new Obra();
        obraFinalizada.setId(3);
        obraFinalizada.setDireccion("Dirección 3");
        obraFinalizada.setPresupuesto(BigDecimal.valueOf(20000));
        obraFinalizada.setEstado(EstadoObra.FINALIZADA);
        obraFinalizada.setCliente(clienteConLimite);

        // Usuario
        usuarioTest = new Usuario();
        usuarioTest.setId(1);
        usuarioTest.setNombre("Test");
        usuarioTest.setApellido("Usuario");
        usuarioTest.setClientes(new ArrayList<>());
    }

    // ========== Tests de CRUD Básico ==========

    @Test
    void findAll_DeberiaRetornarTodasLasObras() {
        // Given
        List<Obra> obras = Arrays.asList(obraHabilitada, obraPendiente, obraFinalizada);
        when(obraRepository.findAll()).thenReturn(obras);

        // When
        List<Obra> resultado = obraService.findAll();

        // Then
        assertThat(resultado).hasSize(3);
        verify(obraRepository).findAll();
    }

    @Test
    void findById_ConIdExistente_DeberiaRetornarObra() {
        // Given
        when(obraRepository.findById(1)).thenReturn(Optional.of(obraHabilitada));

        // When
        Optional<Obra> resultado = obraService.findById(1);

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1);
        verify(obraRepository).findById(1);
    }

    @Test
    void save_DeberiaGuardarObra() {
        // Given
        when(obraRepository.save(any(Obra.class))).thenReturn(obraHabilitada);

        // When
        Obra resultado = obraService.save(obraHabilitada);

        // Then
        assertThat(resultado).isNotNull();
        verify(obraRepository).save(obraHabilitada);
    }

    @Test
    void deleteById_DeberiaEliminarObra() {
        // Given
        doNothing().when(obraRepository).deleteById(1);

        // When
        obraService.deleteById(1);

        // Then
        verify(obraRepository).deleteById(1);
    }

    // ========== Tests de Regla de Negocio: Finalizar Obra ==========

    @Test
    void finalizarObra_ConObraHabilitada_DeberiaFinalizarYHabilitarPendientes() {
        // Given
        when(obraRepository.findById(1)).thenReturn(Optional.of(obraHabilitada));
        when(obraRepository.save(any(Obra.class))).thenReturn(obraFinalizada);
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(1L);
        when(obraRepository.findByClienteAndEstadoOrderByIdAsc(clienteConLimite, EstadoObra.PENDIENTE))
            .thenReturn(Arrays.asList(obraPendiente));

        // When
        Obra resultado = obraService.finalizarObra(1);

        // Then
        assertThat(resultado).isNotNull();
        verify(obraRepository, times(2)).save(any(Obra.class)); // Se guarda la obra finalizada y la pendiente habilitada
        verify(obraRepository).countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA);
        verify(obraRepository).findByClienteAndEstadoOrderByIdAsc(clienteConLimite, EstadoObra.PENDIENTE);
    }

    @Test
    void finalizarObra_ConObraInexistente_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> obraService.finalizarObra(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Obra no encontrada");

        verify(obraRepository).findById(999);
        verify(obraRepository, never()).save(any());
    }

    @Test
    void finalizarObra_ConObraYaFinalizada_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(3)).thenReturn(Optional.of(obraFinalizada));

        // When & Then
        assertThatThrownBy(() -> obraService.finalizarObra(3))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("ya está finalizada");

        verify(obraRepository).findById(3);
        verify(obraRepository, never()).save(any());
    }

    // ========== Tests de Regla de Negocio: Pasar a Pendiente ==========

    @Test
    void pasarAPendiente_ConObraHabilitada_DeberiaCambiarEstado() {
        // Given
        when(obraRepository.findById(1)).thenReturn(Optional.of(obraHabilitada));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Obra resultado = obraService.pasarAPendiente(1);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoObra.PENDIENTE);
        verify(obraRepository).save(any(Obra.class));
    }

    @Test
    void pasarAPendiente_ConObraYaPendiente_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(2)).thenReturn(Optional.of(obraPendiente));

        // When & Then
        assertThatThrownBy(() -> obraService.pasarAPendiente(2))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("ya está en estado pendiente");

        verify(obraRepository, never()).save(any());
    }

    @Test
    void pasarAPendiente_ConObraFinalizada_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(3)).thenReturn(Optional.of(obraFinalizada));

        // When & Then
        assertThatThrownBy(() -> obraService.pasarAPendiente(3))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No se puede pasar una obra finalizada a pendiente");

        verify(obraRepository, never()).save(any());
    }

    // ========== Tests de Regla de Negocio: Pasar a Habilitada ==========

    @Test
    void pasarAHabilitada_ConEspacioDisponible_DeberiaHabilitar() {
        // Given
        when(obraRepository.findById(2)).thenReturn(Optional.of(obraPendiente));
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(1L);
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Obra resultado = obraService.pasarAHabilitada(2);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoObra.HABILITADA);
        verify(obraRepository).countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA);
        verify(obraRepository).save(any(Obra.class));
    }

    @Test
    void pasarAHabilitada_SinEspacioDisponible_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(2)).thenReturn(Optional.of(obraPendiente));
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(2L);

        // When & Then
        assertThatThrownBy(() -> obraService.pasarAHabilitada(2))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("ha alcanzado el máximo de obras en ejecución");

        verify(obraRepository).countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA);
        verify(obraRepository, never()).save(any());
    }

    @Test
    void pasarAHabilitada_ConObraYaHabilitada_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(1)).thenReturn(Optional.of(obraHabilitada));

        // When & Then
        assertThatThrownBy(() -> obraService.pasarAHabilitada(1))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("ya está habilitada");

        verify(obraRepository, never()).save(any());
    }

    @Test
    void pasarAHabilitada_ConObraFinalizada_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(3)).thenReturn(Optional.of(obraFinalizada));

        // When & Then
        assertThatThrownBy(() -> obraService.pasarAHabilitada(3))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No se puede habilitar una obra finalizada");

        verify(obraRepository, never()).save(any());
    }

    @Test
    void pasarAHabilitada_ConClienteSinLimite_DeberiaHabilitar() {
        // Given
        obraPendiente.setCliente(clienteSinLimite);
        when(obraRepository.findById(2)).thenReturn(Optional.of(obraPendiente));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Obra resultado = obraService.pasarAHabilitada(2);

        // Then
        assertThat(resultado.getEstado()).isEqualTo(EstadoObra.HABILITADA);
        verify(obraRepository, never()).countByClienteAndEstado(any(), any());
        verify(obraRepository).save(any(Obra.class));
    }

    // ========== Tests de Regla de Negocio: Asignar Cliente ==========

    @Test
    void asignarCliente_ConEspacioDisponible_DeberiaAsignarYHabilitar() {
        // Given
        Obra obraSinCliente = new Obra();
        obraSinCliente.setId(4);
        obraSinCliente.setDireccion("Dirección 4");
        obraSinCliente.setPresupuesto(BigDecimal.valueOf(5000));
        obraSinCliente.setEstado(EstadoObra.PENDIENTE);

        when(obraRepository.findById(4)).thenReturn(Optional.of(obraSinCliente));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteConLimite));
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(1L);
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Obra resultado = obraService.asignarCliente(4, 1);

        // Then
        assertThat(resultado.getCliente()).isEqualTo(clienteConLimite);
        assertThat(resultado.getEstado()).isEqualTo(EstadoObra.HABILITADA);
        verify(obraRepository).save(any(Obra.class));
    }

    @Test
    void asignarCliente_SinEspacioDisponible_DeberiaAsignarYDejarPendiente() {
        // Given
        Obra obraSinCliente = new Obra();
        obraSinCliente.setId(4);
        obraSinCliente.setDireccion("Dirección 4");
        obraSinCliente.setPresupuesto(BigDecimal.valueOf(5000));
        obraSinCliente.setEstado(EstadoObra.PENDIENTE);

        when(obraRepository.findById(4)).thenReturn(Optional.of(obraSinCliente));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteConLimite));
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(2L);
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Obra resultado = obraService.asignarCliente(4, 1);

        // Then
        assertThat(resultado.getCliente()).isEqualTo(clienteConLimite);
        assertThat(resultado.getEstado()).isEqualTo(EstadoObra.PENDIENTE);
        verify(obraRepository).save(any(Obra.class));
    }

    @Test
    void asignarCliente_ConClienteSinLimite_DeberiaAsignarYHabilitar() {
        // Given
        Obra obraSinCliente = new Obra();
        obraSinCliente.setId(4);
        obraSinCliente.setDireccion("Dirección 4");
        obraSinCliente.setPresupuesto(BigDecimal.valueOf(5000));
        obraSinCliente.setEstado(EstadoObra.PENDIENTE);

        when(obraRepository.findById(4)).thenReturn(Optional.of(obraSinCliente));
        when(clienteRepository.findById(2)).thenReturn(Optional.of(clienteSinLimite));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Obra resultado = obraService.asignarCliente(4, 2);

        // Then
        assertThat(resultado.getCliente()).isEqualTo(clienteSinLimite);
        assertThat(resultado.getEstado()).isEqualTo(EstadoObra.HABILITADA);
        verify(obraRepository, never()).countByClienteAndEstado(any(), any());
        verify(obraRepository).save(any(Obra.class));
    }

    @Test
    void asignarCliente_AObraFinalizada_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(3)).thenReturn(Optional.of(obraFinalizada));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteConLimite));

        // When & Then
        assertThatThrownBy(() -> obraService.asignarCliente(3, 1))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("No se puede asignar un cliente a una obra finalizada");

        verify(obraRepository, never()).save(any());
    }

    @Test
    void asignarCliente_ConClienteInexistente_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(1)).thenReturn(Optional.of(obraHabilitada));
        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> obraService.asignarCliente(1, 999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cliente no encontrado");

        verify(obraRepository, never()).save(any());
    }

    @Test
    void asignarCliente_ConObraInexistente_DeberiaLanzarExcepcion() {
        // Given
        when(obraRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> obraService.asignarCliente(999, 1))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Obra no encontrada");

        verify(clienteRepository, never()).findById(any());
        verify(obraRepository, never()).save(any());
    }

    // ========== Tests de Regla de Negocio: Verificar y Habilitar Obras Pendientes ==========

    @Test
    void verificarYHabilitarObrasPendientes_ConEspacioYPendientes_DeberiaHabilitarObras() {
        // Given
        Obra obraPendiente1 = new Obra();
        obraPendiente1.setId(4);
        obraPendiente1.setEstado(EstadoObra.PENDIENTE);
        obraPendiente1.setCliente(clienteConLimite);

        Obra obraPendiente2 = new Obra();
        obraPendiente2.setId(5);
        obraPendiente2.setEstado(EstadoObra.PENDIENTE);
        obraPendiente2.setCliente(clienteConLimite);

        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(0L);
        when(obraRepository.findByClienteAndEstadoOrderByIdAsc(clienteConLimite, EstadoObra.PENDIENTE))
            .thenReturn(Arrays.asList(obraPendiente1, obraPendiente2));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        obraService.verificarYHabilitarObrasPendientes(clienteConLimite);

        // Then
        verify(obraRepository, times(2)).save(any(Obra.class)); // Habilita 2 obras (el límite es 2)
    }

    @Test
    void verificarYHabilitarObrasPendientes_SinEspacio_NoDeberiaHabilitarObras() {
        // Given
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(2L);
        when(obraRepository.findByClienteAndEstadoOrderByIdAsc(clienteConLimite, EstadoObra.PENDIENTE))
            .thenReturn(Arrays.asList(obraPendiente));

        // When
        obraService.verificarYHabilitarObrasPendientes(clienteConLimite);

        // Then
        verify(obraRepository, never()).save(any());
    }

    @Test
    void verificarYHabilitarObrasPendientes_SinPendientes_NoDeberiaHacerNada() {
        // Given
        when(obraRepository.countByClienteAndEstado(clienteConLimite, EstadoObra.HABILITADA)).thenReturn(1L);
        when(obraRepository.findByClienteAndEstadoOrderByIdAsc(clienteConLimite, EstadoObra.PENDIENTE))
            .thenReturn(Collections.emptyList());

        // When
        obraService.verificarYHabilitarObrasPendientes(clienteConLimite);

        // Then
        verify(obraRepository, never()).save(any());
    }

    @Test
    void verificarYHabilitarObrasPendientes_ConClienteNulo_NoDeberiaHacerNada() {
        // When
        obraService.verificarYHabilitarObrasPendientes(null);

        // Then
        verify(obraRepository, never()).countByClienteAndEstado(any(), any());
        verify(obraRepository, never()).findByClienteAndEstadoOrderByIdAsc(any(), any());
        verify(obraRepository, never()).save(any());
    }

    @Test
    void verificarYHabilitarObrasPendientes_ConClienteSinLimite_NoDeberiaHacerNada() {
        // When
        obraService.verificarYHabilitarObrasPendientes(clienteSinLimite);

        // Then
        verify(obraRepository, never()).countByClienteAndEstado(any(), any());
        verify(obraRepository, never()).findByClienteAndEstadoOrderByIdAsc(any(), any());
        verify(obraRepository, never()).save(any());
    }

    // ========== Tests de findByUsuarioId ==========

    @Test
    void findByUsuarioId_ConUsuarioConClientes_DeberiaRetornarObrasDeClientes() {
        // Given
        usuarioTest.getClientes().add(clienteConLimite);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));
        when(obraRepository.findByCliente(clienteConLimite))
            .thenReturn(Arrays.asList(obraHabilitada, obraPendiente));

        // When
        List<Obra> resultado = obraService.findByUsuarioId(1);

        // Then
        assertThat(resultado).hasSize(2);
        verify(obraRepository).findByCliente(clienteConLimite);
    }

    @Test
    void findByUsuarioId_ConUsuarioSinClientes_DeberiaRetornarListaVacia() {
        // Given
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));

        // When
        List<Obra> resultado = obraService.findByUsuarioId(1);

        // Then
        assertThat(resultado).isEmpty();
        verify(obraRepository, never()).findByCliente(any());
    }

    @Test
    void findByUsuarioId_ConUsuarioInexistente_DeberiaRetornarListaVacia() {
        // Given
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // When
        List<Obra> resultado = obraService.findByUsuarioId(999);

        // Then
        assertThat(resultado).isEmpty();
        verify(obraRepository, never()).findByCliente(any());
    }
}
