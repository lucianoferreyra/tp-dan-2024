package isi.dan.ms.pedidos.dto;

import java.util.List;

public class OrdenEjecutadaDTO {
  private Long ordenId;
  private String estado;
  private List<ItemOrdenDTO> items;

  // Constructor por defecto
  public OrdenEjecutadaDTO() {
  }

  // Constructor con parámetros
  public OrdenEjecutadaDTO(Long ordenId, String estado, List<ItemOrdenDTO> items) {
    this.ordenId = ordenId;
    this.estado = estado;
    this.items = items;
  }

  // Getters y Setters
  public Long getOrdenId() {
    return ordenId;
  }

  public void setOrdenId(Long ordenId) {
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
}