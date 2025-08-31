package isi.dan.ms_productos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isi.dan.ms_productos.aop.LogExecutionTime;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.servicio.CategoriaService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
  @Autowired
  private CategoriaService categoriaService;

  Logger log = LoggerFactory.getLogger(CategoriaController.class);

  @GetMapping
  @LogExecutionTime
  public ResponseEntity<List<Categoria>> getAllCategorias() {
    log.info("GET /api/categorias");
    List<Categoria> categorias = categoriaService.getAllCategorias();
    return ResponseEntity.ok(categorias);
  }

  @GetMapping("/{id}")
  @LogExecutionTime
  public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) {
    log.info("GET /api/categorias/{}", id);
    try {
      Categoria categoria = categoriaService.getCategoriaById(id);
      return ResponseEntity.ok(categoria);
    } catch (CategoriaNotFoundException e) {
      log.warn("Categoría no encontrada con ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/nombre/{nombre}")
  @LogExecutionTime
  public ResponseEntity<Categoria> getCategoriaByNombre(@PathVariable String nombre) {
    log.info("GET /api/categorias/nombre/{}", nombre);
    try {
      Categoria categoria = categoriaService.getCategoriaByNombre(nombre);
      return ResponseEntity.ok(categoria);
    } catch (CategoriaNotFoundException e) {
      log.warn("Categoría no encontrada con nombre: {}", nombre);
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping
  @LogExecutionTime
  public ResponseEntity<Categoria> createCategoria(@Valid @RequestBody Categoria categoria) {
    log.info("POST /api/categorias - {}", categoria.getNombre());

    // Verificar si ya existe una categoría con ese nombre
    if (categoriaService.existsByNombre(categoria.getNombre())) {
      log.warn("Ya existe una categoría con el nombre: {}", categoria.getNombre());
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    Categoria savedCategoria = categoriaService.saveCategoria(categoria);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCategoria);
  }

  @PutMapping("/{id}")
  @LogExecutionTime
  public ResponseEntity<Categoria> updateCategoria(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
    log.info("PUT /api/categorias/{} - {}", id, categoria.getNombre());
    try {
      // Verificar si el nuevo nombre ya existe en otra categoría
      if (categoriaService.existsByNombre(categoria.getNombre())) {
        Categoria existingCategoria = categoriaService.getCategoriaByNombre(categoria.getNombre());
        if (!existingCategoria.getId().equals(id)) {
          log.warn("Ya existe otra categoría con el nombre: {}", categoria.getNombre());
          return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
      }

      Categoria updatedCategoria = categoriaService.updateCategoria(id, categoria);
      return ResponseEntity.ok(updatedCategoria);
    } catch (CategoriaNotFoundException e) {
      log.warn("Categoría no encontrada con ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  @LogExecutionTime
  public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
    log.info("DELETE /api/categorias/{}", id);
    try {
      categoriaService.deleteCategoria(id);
      return ResponseEntity.noContent().build();
    } catch (CategoriaNotFoundException e) {
      log.warn("Categoría no encontrada con ID: {}", id);
      return ResponseEntity.notFound().build();
    }
  }
}