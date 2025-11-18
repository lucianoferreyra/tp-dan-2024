package isi.dan.ms.pedidos.dto;

import java.util.List;

public class StockDevolucionDTO {
  private String pedidoId;
  private String numeroPedido;
  private String motivo;
  private List<ItemDevolucionDTO> items;

  // Constructor por defecto
  public StockDevolucionDTO() {
  }

  // Constructor con parámetros
  public StockDevolucionDTO(String pedidoId, String numeroPedido, String motivo, List<ItemDevolucionDTO> items) {
    this.pedidoId = pedidoId;
    this.numeroPedido = numeroPedido;
    this.motivo = motivo;
    this.items = items;
  }

  // Getters y Setters
  public String getPedidoId() {
    return pedidoId;
  }

  public void setPedidoId(String pedidoId) {
    this.pedidoId = pedidoId;
  }

  public String getNumeroPedido() {
    return numeroPedido;
  }

  public void setNumeroPedido(String numeroPedido) {
    this.numeroPedido = numeroPedido;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

  public List<ItemDevolucionDTO> getItems() {
    return items;
  }

  public void setItems(List<ItemDevolucionDTO> items) {
    this.items = items;
  }

  public static class ItemDevolucionDTO {
    private Long productoId;
    private Integer cantidad;

    public ItemDevolucionDTO() {
    }

    public ItemDevolucionDTO(Long productoId, Integer cantidad) {
      this.productoId = productoId;
      this.cantidad = cantidad;
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