package isi.dan.msclientes.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.dao.UsuarioRepository;
import isi.dan.msclientes.enums.EstadoObra;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ObraService {

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Obra> findAll() {
        return obraRepository.findAll();
    }

    public List<Obra> findByUsuarioId(Integer usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getClientes() != null && !usuarioOpt.get().getClientes().isEmpty()) {
            List<Obra> todasLasObras = new ArrayList<>();
            for (Cliente cliente : usuarioOpt.get().getClientes()) {
                todasLasObras.addAll(obraRepository.findByCliente(cliente));
            }
            return todasLasObras;
        }
        return Collections.emptyList();
    }

    public List<Obra> findByClienteId(Integer clienteId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isPresent()) {
            return obraRepository.findByCliente(clienteOpt.get());
        }
        return Collections.emptyList();
    }

    public Optional<Obra> findById(Integer id) {
        return obraRepository.findById(id);
    }

    public Obra save(Obra obra) {
        return obraRepository.save(obra);
    }

    public Obra update(Obra obra) {
        return obraRepository.save(obra);
    }

    public void deleteById(Integer id) {
        obraRepository.deleteById(id);
    }

    @Transactional
    public Obra finalizarObra(Integer obraId) {
        Optional<Obra> obraOpt = findById(obraId);
        if (obraOpt.isEmpty()) {
            throw new RuntimeException("Obra no encontrada con ID: " + obraId);
        }

        Obra obra = obraOpt.get();
        if (obra.getEstado() == EstadoObra.FINALIZADA) {
            throw new RuntimeException("La obra ya está finalizada");
        }

        obra.setEstado(EstadoObra.FINALIZADA);
        Obra obraFinalizada = update(obra);

        verificarYHabilitarObrasPendientes(obra.getCliente());

        return obraFinalizada;
    }

    @Transactional
    public void verificarYHabilitarObrasPendientes(Cliente cliente) {
        if (cliente == null || cliente.getMaximoCantidadObrasEnEjecucion() == null) {
            return;
        }

        long obrasActivas = obraRepository.countByClienteAndEstado(cliente, EstadoObra.HABILITADA);

        List<Obra> obrasPendientes = obraRepository.findByClienteAndEstadoOrderByIdAsc(
                cliente, EstadoObra.PENDIENTE);

        for (Obra obraPendiente : obrasPendientes) {
            if (obrasActivas < cliente.getMaximoCantidadObrasEnEjecucion()) {
                obraPendiente.setEstado(EstadoObra.HABILITADA);
                update(obraPendiente);
                obrasActivas++;
            } else {
                break;
            }
        }
    }

    @Transactional
    public Obra pasarAPendiente(Integer obraId) {
        Optional<Obra> obraOpt = findById(obraId);
        if (obraOpt.isEmpty()) {
            throw new RuntimeException("Obra no encontrada con ID: " + obraId);
        }

        Obra obra = obraOpt.get();
        if (obra.getEstado() == EstadoObra.PENDIENTE) {
            throw new RuntimeException("La obra ya está en estado pendiente");
        }

        if (obra.getEstado() == EstadoObra.FINALIZADA) {
            throw new RuntimeException("No se puede pasar una obra finalizada a pendiente");
        }

        obra.setEstado(EstadoObra.PENDIENTE);
        return update(obra);
    }

    @Transactional
    public Obra pasarAHabilitada(Integer obraId) {
        Optional<Obra> obraOpt = findById(obraId);
        if (obraOpt.isEmpty()) {
            throw new RuntimeException("Obra no encontrada con ID: " + obraId);
        }

        Obra obra = obraOpt.get();
        if (obra.getEstado() == EstadoObra.HABILITADA) {
            throw new RuntimeException("La obra ya está habilitada");
        }

        if (obra.getEstado() == EstadoObra.FINALIZADA) {
            throw new RuntimeException("No se puede habilitar una obra finalizada");
        }

        Cliente cliente = obra.getCliente();
        if (cliente == null) {
            throw new RuntimeException("La obra no tiene cliente asignado");
        }

        if (cliente.getMaximoCantidadObrasEnEjecucion() != null) {
            long obrasActivas = obraRepository.countByClienteAndEstado(cliente, EstadoObra.HABILITADA);

            if (obrasActivas >= cliente.getMaximoCantidadObrasEnEjecucion()) {
                throw new RuntimeException("El cliente ha alcanzado el máximo de obras en ejecución (" +
                        cliente.getMaximoCantidadObrasEnEjecucion() +
                        "). Obras activas: " + obrasActivas);
            }
        }

        obra.setEstado(EstadoObra.HABILITADA);
        return update(obra);
    }

    @Transactional
    public Obra asignarCliente(Integer obraId, Integer clienteId) {
        Optional<Obra> obraOpt = findById(obraId);
        if (obraOpt.isEmpty()) {
            throw new RuntimeException("Obra no encontrada con ID: " + obraId);
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);
        }

        Obra obra = obraOpt.get();
        Cliente cliente = clienteOpt.get();

        if (obra.getEstado() == EstadoObra.FINALIZADA) {
            throw new RuntimeException("No se puede asignar un cliente a una obra finalizada");
        }

        obra.setCliente(cliente);

        EstadoObra nuevoEstado;
        if (cliente.getMaximoCantidadObrasEnEjecucion() == null) {
            nuevoEstado = EstadoObra.HABILITADA;
        } else {
            long obrasActivas = obraRepository.countByClienteAndEstado(cliente, EstadoObra.HABILITADA);

            if (obrasActivas < cliente.getMaximoCantidadObrasEnEjecucion()) {
                nuevoEstado = EstadoObra.HABILITADA;
            } else {
                nuevoEstado = EstadoObra.PENDIENTE;
            }
        }

        obra.setEstado(nuevoEstado);
        return update(obra);
    }
}
