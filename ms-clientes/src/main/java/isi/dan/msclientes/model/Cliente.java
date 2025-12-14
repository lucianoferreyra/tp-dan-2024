package isi.dan.msclientes.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "MS_CLI_CLIENTE")
@Data
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(name="CORREO_ELECTRONICO")
    @Email(message = "Email debe ser valido")
    @NotBlank(message = "Email es obligatorio")
    private String correoElectronico;
    
    private String cuit;

    @Column(name="MAXIMO_DESCUBIERTO")
    @Min(value = 1, message = "El descubierto maximo debe ser mayor a 0")
    private BigDecimal maximoDescubierto;

    @Column(name="MAXIMO_OBRAS")
    private Integer maximoCantidadObrasEnEjecucion;
    
    @ManyToMany(mappedBy = "clientes")
    @JsonIgnoreProperties("clientes")
    private List<Usuario> usuarios = new ArrayList<>();
    
}
