package isi.dan.ms_productos.dto;

public class ItemOrdenDTO {
  private Long productoId;
  private Integer cantidad;

  // Constructores
  public ItemOrdenDTO() {
  }

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

  @Override
  public String toString() {
    return "ItemOrdenDTO{" +
        "productoId=" + productoId +
        ", cantidad=" + cantidad +
        '}';
  }
}