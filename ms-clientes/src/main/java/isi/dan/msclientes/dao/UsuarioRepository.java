package isi.dan.msclientes.dao;

import isi.dan.msclientes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    @Query("SELECT u FROM Usuario u JOIN u.clientes c WHERE c.id = :clienteId")
    List<Usuario> findByClienteId(@Param("clienteId") Integer clienteId);
    
    Optional<Usuario> findByDni(String dni);
    
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);
    
    boolean existsByDni(String dni);
    
    boolean existsByCorreoElectronico(String correoElectronico);
}
