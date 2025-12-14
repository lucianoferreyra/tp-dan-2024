package isi.dan.msclientes.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MS_CLI_USUARIO")
@Data
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    
    @Column(unique = true)
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;
    
    @Column(name = "CORREO_ELECTRONICO", unique = true)
    @Email(message = "Email debe ser válido")
    @NotBlank(message = "Email es obligatorio")
    private String correoElectronico;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "MS_CLI_USUARIO_CLIENTE",
        joinColumns = @JoinColumn(name = "ID_USUARIO"),
        inverseJoinColumns = @JoinColumn(name = "ID_CLIENTE")
    )
    @JsonIgnoreProperties("usuarios")
    private List<Cliente> clientes = new ArrayList<>();
}
