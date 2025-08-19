package isi.dan.msclientes.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import isi.dan.msclientes.enums.EstadoObra;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;

@Repository
public interface ObraRepository extends JpaRepository<Obra, Integer> {

    List<Obra> findByPresupuestoGreaterThanEqual(BigDecimal price);

    // Count works by client and status
    long countByClienteAndEstado(Cliente cliente, EstadoObra estado);

    // Find works by client and status
    List<Obra> findByClienteAndEstado(Cliente cliente, EstadoObra estado);

    // Find oldest pending work for a client
    @Query("SELECT o FROM Obra o WHERE o.cliente = :cliente AND o.estado = :estado ORDER BY o.id ASC")
    List<Obra> findByClienteAndEstadoOrderByIdAsc(@Param("cliente") Cliente cliente,
            @Param("estado") EstadoObra estado);

    // Find works by client
    List<Obra> findByCliente(Cliente cliente);

    // Find works by status
    List<Obra> findByEstado(EstadoObra estado);

}
