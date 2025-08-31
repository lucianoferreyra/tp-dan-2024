package isi.dan.ms_productos.servicio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.ms_productos.dao.CategoriaRepository;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;

import java.util.List;

@Service
public class CategoriaService {
  @Autowired
  private CategoriaRepository categoriaRepository;

  Logger log = LoggerFactory.getLogger(CategoriaService.class);

  public Categoria saveCategoria(Categoria categoria) {
    log.info("Guardando categoría: {}", categoria.getNombre());
    return categoriaRepository.save(categoria);
  }

  public List<Categoria> getAllCategorias() {
    log.info("Obteniendo todas las categorías");
    return categoriaRepository.findAll();
  }

  public Categoria getCategoriaById(Long id) throws CategoriaNotFoundException {
    log.info("Buscando categoría con ID: {}", id);
    return categoriaRepository.findById(id)
        .orElseThrow(() -> new CategoriaNotFoundException(id));
  }

  public Categoria getCategoriaByNombre(String nombre) throws CategoriaNotFoundException {
    log.info("Buscando categoría con nombre: {}", nombre);
    return categoriaRepository.findByNombre(nombre)
        .orElseThrow(() -> new CategoriaNotFoundException(nombre));
  }

  public Categoria updateCategoria(Long id, Categoria categoria) throws CategoriaNotFoundException {
    log.info("Actualizando categoría con ID: {}", id);
    Categoria existingCategoria = getCategoriaById(id);
    existingCategoria.setNombre(categoria.getNombre());
    return categoriaRepository.save(existingCategoria);
  }

  public void deleteCategoria(Long id) throws CategoriaNotFoundException {
    log.info("Eliminando categoría con ID: {}", id);
    if (!categoriaRepository.existsById(id)) {
      throw new CategoriaNotFoundException(id);
    }
    categoriaRepository.deleteById(id);
  }

  public boolean existsByNombre(String nombre) {
    return categoriaRepository.existsByNombre(nombre);
  }
}
