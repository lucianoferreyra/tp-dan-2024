package isi.dan.ms_productos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoUpdateDTO {
  @NotBlank(message = "El nombre del producto es obligatorio")
  private String nombre;

  private String descripcion;

  @NotNull(message = "El precio es obligatorio")
  @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
  private BigDecimal precio;

  @NotNull(message = "El descuento promocional es obligatorio")
  @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
  @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
  private BigDecimal descuentoPromocional;

  @NotNull(message = "El stock mínimo es obligatorio")
  @Min(value = 0, message = "El stock mínimo no puede ser negativo")
  private Integer stockMinimo;

  @NotNull(message = "La categoría es obligatoria")
  private Long categoriaId;
}