package isi.dan.ms.pedidos.modelo;

import org.springframework.data.mongodb.core.mapping.Field;
import java.math.BigDecimal;

public class DetallePedido {

    @Field("producto_id")
    private Long productoId;

    @Field("cantidad")
    private Integer cantidad;

    @Field("precio_unitario")
    private BigDecimal precioUnitario;

    @Field("monto_linea")
    private BigDecimal montoLinea;

    // Constructor por defecto
    public DetallePedido() {
    }

    // Constructor con parámetros
    public DetallePedido(Long productoId, Integer cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.montoLinea = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
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

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getMontoLinea() {
        return montoLinea;
    }

    public void setMontoLinea(BigDecimal montoLinea) {
        this.montoLinea = montoLinea;
    }

    // Método para calcular el monto de la línea
    public void calcularMontoLinea() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.montoLinea = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        }
    }
}