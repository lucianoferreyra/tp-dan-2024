package isi.dan.ms_productos.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
// import java.util.List;

@Entity
@Table(name = "MS_PRD_PRODUCTO")
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @Column(name = "descuento_promocional", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "El descuento promocional es obligatorio")
    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
    private BigDecimal descuentoPromocional = BigDecimal.ZERO;

    @Column(name = "stock_actual", nullable = false)
    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    // Relación con Categoria
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoría es obligatoria")
    private Categoria categoria;

    // Constructor sin argumentos
    public Producto() {
        this.stockActual = 0;
        this.descuentoPromocional = BigDecimal.ZERO;
    }

    // Constructor para alta de producto
    public Producto(String nombre, String descripcion, Categoria categoria,
            Integer stockMinimo, BigDecimal precio, BigDecimal descuentoPromocional) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.stockMinimo = stockMinimo;
        this.precio = precio;
        this.descuentoPromocional = descuentoPromocional != null ? descuentoPromocional : BigDecimal.ZERO;
        this.stockActual = 0; // Stock inicial siempre 0
    }

    // Método para calcular precio con descuento
    public BigDecimal getPrecioConDescuento() {
        if (descuentoPromocional == null || descuentoPromocional.compareTo(BigDecimal.ZERO) == 0) {
            return precio;
        }
        BigDecimal descuento = precio.multiply(descuentoPromocional).divide(new BigDecimal("100"));
        return precio.subtract(descuento);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getDescuentoPromocional() {
        return descuentoPromocional;
    }

    public void setDescuentoPromocional(BigDecimal descuentoPromocional) {
        this.descuentoPromocional = descuentoPromocional != null ? descuentoPromocional : BigDecimal.ZERO;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", stockActual=" + stockActual +
                ", stockMinimo=" + stockMinimo +
                ", categoria=" + (categoria != null ? categoria.getNombre() : "null") +
                '}';
    }
}
