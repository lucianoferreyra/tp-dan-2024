package isi.dan.ms.pedidos.dto;

import java.util.List;

public class CrearPedidoDTO {

  private Long clienteId;
  private Long obraId;
  private String observaciones;
  private List<DetallePedidoDTO> detalles;

  // Constructor por defecto
  public CrearPedidoDTO() {
  }

  // Getters y Setters
  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long clienteId) {
    this.clienteId = clienteId;
  }

  public Long getObraId() {
    return obraId;
  }

  public void setObraId(Long obraId) {
    this.obraId = obraId;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public List<DetallePedidoDTO> getDetalles() {
    return detalles;
  }

  public void setDetalles(List<DetallePedidoDTO> detalles) {
    this.detalles = detalles;
  }

  public static class DetallePedidoDTO {
    private Long productoId;
    private Integer cantidad;

    public DetallePedidoDTO() {
    }

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
}