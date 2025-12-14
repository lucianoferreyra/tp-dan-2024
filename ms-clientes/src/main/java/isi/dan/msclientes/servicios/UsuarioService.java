package isi.dan.msclientes.servicios;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.dao.UsuarioRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.DniDuplicadoException;
import isi.dan.msclientes.exception.EmailUsuarioDuplicadoException;
import isi.dan.msclientes.exception.UsuarioNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @LogExecutionTime
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @LogExecutionTime
    public List<Usuario> findByClienteId(Integer clienteId) {
        return usuarioRepository.findByClienteId(clienteId);
    }

    @LogExecutionTime
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    @LogExecutionTime
    public Usuario save(Usuario usuario) throws ClienteNotFoundException, DniDuplicadoException, EmailUsuarioDuplicadoException {
        // Validar que tenga al menos un cliente
        if (usuario.getClientes() == null || usuario.getClientes().isEmpty()) {
            throw new ClienteNotFoundException("El usuario debe tener al menos un cliente asociado");
        }

        // Validar que todos los clientes existan
        for (Cliente cliente : usuario.getClientes()) {
            if (cliente.getId() == null) {
                throw new ClienteNotFoundException("El ID del cliente es obligatorio");
            }
            Optional<Cliente> clienteOpt = clienteRepository.findById(cliente.getId());
            if (!clienteOpt.isPresent()) {
                throw new ClienteNotFoundException("Cliente no encontrado con ID: " + cliente.getId());
            }
        }

        // Validar que el DNI no esté duplicado
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new DniDuplicadoException("Ya existe un usuario con el DNI: " + usuario.getDni());
        }

        // Validar que el email no esté duplicado
        if (usuarioRepository.existsByCorreoElectronico(usuario.getCorreoElectronico())) {
            throw new EmailUsuarioDuplicadoException("Ya existe un usuario con el email: " + usuario.getCorreoElectronico());
        }

        log.info("Creando usuario {} {} con {} cliente(s)", 
                usuario.getNombre(), usuario.getApellido(), usuario.getClientes().size());
        return usuarioRepository.save(usuario);
    }

    @LogExecutionTime
    public Usuario update(Integer id, Usuario usuarioActualizado) throws UsuarioNotFoundException, DniDuplicadoException, EmailUsuarioDuplicadoException, ClienteNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (!usuarioOpt.isPresent()) {
            throw new UsuarioNotFoundException("Usuario no encontrado con ID: " + id);
        }

        Usuario usuarioExistente = usuarioOpt.get();

        // Validar DNI duplicado (solo si cambió)
        if (!usuarioExistente.getDni().equals(usuarioActualizado.getDni())) {
            if (usuarioRepository.existsByDni(usuarioActualizado.getDni())) {
                throw new DniDuplicadoException("Ya existe un usuario con el DNI: " + usuarioActualizado.getDni());
            }
        }

        // Validar email duplicado (solo si cambió)
        if (!usuarioExistente.getCorreoElectronico().equals(usuarioActualizado.getCorreoElectronico())) {
            if (usuarioRepository.existsByCorreoElectronico(usuarioActualizado.getCorreoElectronico())) {
                throw new EmailUsuarioDuplicadoException("Ya existe un usuario con el email: " + usuarioActualizado.getCorreoElectronico());
            }
        }

        // Validar que todos los clientes existan si se actualizan
        if (usuarioActualizado.getClientes() != null && !usuarioActualizado.getClientes().isEmpty()) {
            for (Cliente cliente : usuarioActualizado.getClientes()) {
                if (cliente.getId() == null) {
                    throw new ClienteNotFoundException("El ID del cliente es obligatorio");
                }
                Optional<Cliente> clienteOpt = clienteRepository.findById(cliente.getId());
                if (!clienteOpt.isPresent()) {
                    throw new ClienteNotFoundException("Cliente no encontrado con ID: " + cliente.getId());
                }
            }
            usuarioExistente.setClientes(usuarioActualizado.getClientes());
        }

        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setApellido(usuarioActualizado.getApellido());
        usuarioExistente.setDni(usuarioActualizado.getDni());
        usuarioExistente.setCorreoElectronico(usuarioActualizado.getCorreoElectronico());

        log.info("Actualizando usuario {} {} (ID: {})", 
                usuarioExistente.getNombre(), usuarioExistente.getApellido(), id);
        return usuarioRepository.save(usuarioExistente);
    }

    @LogExecutionTime
    public void deleteById(Integer id) throws UsuarioNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (!usuarioOpt.isPresent()) {
            throw new UsuarioNotFoundException("Usuario no encontrado con ID: " + id);
        }
        log.info("Eliminando usuario con ID: {}", id);
        usuarioRepository.deleteById(id);
    }

    public Optional<Usuario> findByDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    public Optional<Usuario> findByCorreoElectronico(String correoElectronico) {
        return usuarioRepository.findByCorreoElectronico(correoElectronico);
    }

    @LogExecutionTime
    public List<Cliente> getClientesDelUsuario(Integer usuarioId) throws UsuarioNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (!usuarioOpt.isPresent()) {
            throw new UsuarioNotFoundException("Usuario no encontrado con ID: " + usuarioId);
        }
        return usuarioOpt.get().getClientes();
    }
}
