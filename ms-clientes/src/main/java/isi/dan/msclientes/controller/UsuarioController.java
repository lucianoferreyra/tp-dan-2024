package isi.dan.msclientes.controller;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.DniDuplicadoException;
import isi.dan.msclientes.exception.EmailUsuarioDuplicadoException;
import isi.dan.msclientes.exception.UsuarioNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Usuario;
import isi.dan.msclientes.servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @LogExecutionTime
    public List<Usuario> getAll() {
        return usuarioService.findAll();
    }

    @GetMapping("/cliente/{clienteId}")
    @LogExecutionTime
    public List<Usuario> getByClienteId(@PathVariable Integer clienteId) {
        return usuarioService.findByClienteId(clienteId);
    }

    @GetMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Usuario> getById(@PathVariable Integer id) throws UsuarioNotFoundException {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return ResponseEntity.ok(
                usuario.orElseThrow(() -> new UsuarioNotFoundException("Usuario " + id + " no encontrado")));
    }

    @GetMapping("/buscar/dni/{dni}")
    @LogExecutionTime
    public ResponseEntity<Usuario> getByDni(@PathVariable String dni) throws UsuarioNotFoundException {
        Optional<Usuario> usuario = usuarioService.findByDni(dni);
        return ResponseEntity.ok(
                usuario.orElseThrow(() -> new UsuarioNotFoundException("Usuario con DNI " + dni + " no encontrado")));
    }

    @GetMapping("/buscar/email/{email}")
    @LogExecutionTime
    public ResponseEntity<Usuario> getByEmail(@PathVariable String email) throws UsuarioNotFoundException {
        Optional<Usuario> usuario = usuarioService.findByCorreoElectronico(email);
        return ResponseEntity.ok(
                usuario.orElseThrow(
                        () -> new UsuarioNotFoundException("Usuario con email " + email + " no encontrado")));
    }

    @PostMapping
    @LogExecutionTime
    public ResponseEntity<Usuario> create(@RequestBody @Validated Usuario usuario)
            throws ClienteNotFoundException, DniDuplicadoException, EmailUsuarioDuplicadoException {
        Usuario nuevoUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @PutMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Usuario> update(@PathVariable Integer id, @RequestBody @Validated Usuario usuario)
            throws UsuarioNotFoundException, DniDuplicadoException, EmailUsuarioDuplicadoException {
        try {

            Usuario usuarioActualizado = usuarioService.update(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (ClienteNotFoundException e) {
            log.error("Error al actualizar usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Void> delete(@PathVariable Integer id) throws UsuarioNotFoundException {
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/cliente")
    @LogExecutionTime
    public ResponseEntity<List<Cliente>> getClientesDelUsuario(@PathVariable Integer id)
            throws UsuarioNotFoundException {
        List<Cliente> clientes = usuarioService.getClientesDelUsuario(id);
        return ResponseEntity.ok(clientes);
    }
}
