package isi.dan.ms.pedidos.dto;

import java.math.BigDecimal;

public class ProductoDTO {
  private Long id;
  private String nombre;
  private String descripcion;
  private BigDecimal precio;
  private Integer stockActual;
  private Integer stockMinimo;
  private Boolean descontinuado;

  // Constructor por defecto
  public ProductoDTO() {
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

  public Boolean getDescontinuado() {
    return descontinuado;
  }

  public void setDescontinuado(Boolean descontinuado) {
    this.descontinuado = descontinuado;
  }
}