package isi.dan.ms_productos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DescuentoPromocionalDTO {

  @NotNull(message = "El descuento promocional es obligatorio")
  @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
  @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
  private BigDecimal descuentoPromocional;

  // Constructores
  public DescuentoPromocionalDTO() {
  }

  public DescuentoPromocionalDTO(BigDecimal descuentoPromocional) {
    this.descuentoPromocional = descuentoPromocional;
  }

  // Getters y Setters
  public BigDecimal getDescuentoPromocional() {
    return descuentoPromocional;
  }

  public void setDescuentoPromocional(BigDecimal descuentoPromocional) {
    this.descuentoPromocional = descuentoPromocional;
  }

  @Override
  public String toString() {
    return "DescuentoPromocionalDTO{" +
        "descuentoPromocional=" + descuentoPromocional +
        '}';
  }
}