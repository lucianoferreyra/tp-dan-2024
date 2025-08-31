package isi.dan.ms_productos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductoCreateDTO {

  @NotBlank(message = "El nombre del producto es obligatorio")
  private String nombre;

  private String descripcion;

  @NotNull(message = "La categoría es obligatoria")
  private Long categoriaId;

  @NotNull(message = "El stock mínimo es obligatorio")
  @Min(value = 0, message = "El stock mínimo no puede ser negativo")
  private Integer stockMinimo;

  @NotNull(message = "El precio es obligatorio")
  @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
  private BigDecimal precio;

  @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
  @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
  private BigDecimal descuentoPromocional = BigDecimal.ZERO;

  // Constructores
  public ProductoCreateDTO() {
  }

  public ProductoCreateDTO(String nombre, String descripcion, Long categoriaId,
      Integer stockMinimo, BigDecimal precio, BigDecimal descuentoPromocional) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.categoriaId = categoriaId;
    this.stockMinimo = stockMinimo;
    this.precio = precio;
    this.descuentoPromocional = descuentoPromocional != null ? descuentoPromocional : BigDecimal.ZERO;
  }

  // Getters y Setters
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

  public Long getCategoriaId() {
    return categoriaId;
  }

  public void setCategoriaId(Long categoriaId) {
    this.categoriaId = categoriaId;
  }

  public Integer getStockMinimo() {
    return stockMinimo;
  }

  public void setStockMinimo(Integer stockMinimo) {
    this.stockMinimo = stockMinimo;
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
}