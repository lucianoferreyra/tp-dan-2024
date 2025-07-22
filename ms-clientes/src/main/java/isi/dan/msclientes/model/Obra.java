package isi.dan.msclientes.model;

import java.math.BigDecimal;

import isi.dan.msclientes.enums.EstadoObra;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "MS_CLI_OBRA")
@Data
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String direccion;

    @Column(name = "ES_REMODELACION")
    private Boolean esRemodelacion;
    
    private float lat;
    
    private float lng;
    
    @ManyToOne
    @JoinColumn(name = "ID_CLIENTE")
    private Cliente cliente;
    
    @NotNull(message = "El presupuesto es obligatorio")
    @Min(value=100, message = "El presupuesto debe ser al menos de 100")
    private BigDecimal presupuesto;

    @Enumerated(EnumType.STRING)
    private EstadoObra estado;
}
