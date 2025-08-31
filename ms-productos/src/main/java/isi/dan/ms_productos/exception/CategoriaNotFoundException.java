package isi.dan.ms_productos.exception;

public class CategoriaNotFoundException extends Exception {
  public CategoriaNotFoundException(Long id) {
    super("Categoría no encontrada con ID: " + id);
  }

  public CategoriaNotFoundException(String nombre) {
    super("Categoría no encontrada con nombre: " + nombre);
  }
}
