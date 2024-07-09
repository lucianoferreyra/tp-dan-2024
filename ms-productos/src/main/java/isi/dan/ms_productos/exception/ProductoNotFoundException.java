package isi.dan.ms_productos.exception;

public class ProductoNotFoundException extends Exception{
    public ProductoNotFoundException(Long id){
        super("Producto "+id+" no encontrado");
    }
}
