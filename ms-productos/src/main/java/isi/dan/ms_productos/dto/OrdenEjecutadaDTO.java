package isi.dan.ms_productos.dto;

import java.util.List;

public class OrdenEjecutadaDTO {
  private String ordenId;
  private String estado;
  private List<ItemOrdenDTO> items;

  // Constructores
  public OrdenEjecutadaDTO() {
  }

  public OrdenEjecutadaDTO(String ordenId, String estado, List<ItemOrdenDTO> items) {
    this.ordenId = ordenId;
    this.estado = estado;
    this.items = items;
  }

  // Getters y Setters
  public String getOrdenId() {
    return ordenId;
  }

  public void setOrdenId(String ordenId) {
    this.ordenId = ordenId;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public List<ItemOrdenDTO> getItems() {
    return items;
  }

  public void setItems(List<ItemOrdenDTO> items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "OrdenEjecutadaDTO{" +
        "ordenId=" + ordenId +
        ", estado='" + estado + '\'' +
        ", items=" + items +
        '}';
  }
}