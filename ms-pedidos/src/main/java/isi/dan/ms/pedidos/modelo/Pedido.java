package isi.dan.ms.pedidos.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id; // Cambiar de Long a String

    @Field("numero_pedido")
    private String numeroPedido;

    @Field("fecha_pedido")
    private LocalDateTime fechaPedido;

    @Field("cliente_id")
    private Long clienteId;

    @Field("obra_id")
    private Long obraId;

    @Field("observaciones")
    private String observaciones;

    @Field("monto_total")
    private BigDecimal montoTotal;

    @Field("estado")
    private EstadoPedido estado;

    @Field("detalles")
    private List<DetallePedido> detalles;

    // Constructor por defecto
    public Pedido() {
        this.fechaPedido = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
    }

    // Getters y Setters
    public String getId() { // Cambiar retorno de Long a String
        return id;
    }

    public void setId(String id) { // Cambiar parámetro de Long a String
        this.id = id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

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

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    public enum EstadoPedido {
        PENDIENTE, ACEPTADO, EN_PREPARACION, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO, RECHAZADO, RECIBIDO
    }
}