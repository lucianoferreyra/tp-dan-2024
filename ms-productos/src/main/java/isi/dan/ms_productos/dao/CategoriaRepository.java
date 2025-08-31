package isi.dan.ms_productos.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import isi.dan.ms_productos.modelo.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
  Optional<Categoria> findByNombre(String nombre);

  boolean existsByNombre(String nombre);
}
