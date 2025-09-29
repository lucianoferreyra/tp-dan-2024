package isi.dan.ms.pedidos.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "monto_linea", precision = 19, scale = 2)
    private BigDecimal montoLinea;

    // Constructor por defecto
    public DetallePedido() {
    }

    // Constructor con parámetros
    public DetallePedido(Producto producto, Integer cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.montoLinea = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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