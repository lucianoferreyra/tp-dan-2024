package isi.dan.ms.pedidos.dto;

public class ItemOrdenDTO {
  private Long productoId;
  private Integer cantidad;

  // Constructor por defecto
  public ItemOrdenDTO() {
  }

  // Constructor con parámetros
  public ItemOrdenDTO(Long productoId, Integer cantidad) {
    this.productoId = productoId;
    this.cantidad = cantidad;
  }

  // Getters y Setters
  public Long getProductoId() {
    return productoId;
  }

  public void setProductoId(Long productoId) {
    this.productoId = productoId;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}