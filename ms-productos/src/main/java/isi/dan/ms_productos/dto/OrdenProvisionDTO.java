package isi.dan.ms_productos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrdenProvisionDTO {

  @NotNull(message = "El ID del producto es obligatorio")
  private Long idProducto;

  @NotNull(message = "La cantidad recibida es obligatoria")
  @Min(value = 1, message = "La cantidad debe ser mayor a 0")
  private Integer cantidadRecibida;

  @NotNull(message = "El precio es obligatorio")
  @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
  private BigDecimal precio;

  // Constructores
  public OrdenProvisionDTO() {
  }

  public OrdenProvisionDTO(Long idProducto, Integer cantidadRecibida, BigDecimal precio) {
    this.idProducto = idProducto;
    this.cantidadRecibida = cantidadRecibida;
    this.precio = precio;
  }

  // Getters y Setters
  public Long getIdProducto() {
    return idProducto;
  }

  public void setIdProducto(Long idProducto) {
    this.idProducto = idProducto;
  }

  public Integer getCantidadRecibida() {
    return cantidadRecibida;
  }

  public void setCantidadRecibida(Integer cantidadRecibida) {
    this.cantidadRecibida = cantidadRecibida;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  @Override
  public String toString() {
    return "OrdenProvisionDTO{" +
        "idProducto=" + idProducto +
        ", cantidadRecibida=" + cantidadRecibida +
        ", precio=" + precio +
        '}';
  }
}