package isi.dan.msclientes.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.enums.EstadoObra;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;

import java.util.List;
import java.util.Optional;

@Service
public class ObraService {

    @Autowired
    private ObraRepository obraRepository;

    public List<Obra> findAll() {
        return obraRepository.findAll();
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

    /**
     * Finalizes a work and checks if any pending work can be enabled
     */
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

        // Finalize the work
        obra.setEstado(EstadoObra.FINALIZADA);
        Obra obraFinalizada = update(obra);

        // Check if we can enable pending works for this client
        verificarYHabilitarObrasPendientes(obra.getCliente());

        return obraFinalizada;
    }

    @Transactional
    public void verificarYHabilitarObrasPendientes(Cliente cliente) {
        if (cliente == null || cliente.getMaximoCantidadObrasEnEjecucion() == null) {
            return;
        }

        // Count current active works
        long obrasActivas = obraRepository.countByClienteAndEstado(cliente, EstadoObra.HABILITADA);

        // Get pending works ordered by creation (FIFO)
        List<Obra> obrasPendientes = obraRepository.findByClienteAndEstadoOrderByIdAsc(
                cliente, EstadoObra.PENDIENTE);

        // Enable pending works while under the limit
        for (Obra obraPendiente : obrasPendientes) {
            if (obrasActivas < cliente.getMaximoCantidadObrasEnEjecucion()) {
                obraPendiente.setEstado(EstadoObra.HABILITADA);
                update(obraPendiente);
                obrasActivas++;
            } else {
                break; // Reached the limit
            }
        }
    }
}
