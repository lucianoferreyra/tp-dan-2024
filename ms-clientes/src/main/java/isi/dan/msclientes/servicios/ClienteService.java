package isi.dan.msclientes.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.dao.UsuarioRepository;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.model.Usuario;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Value("${dan.clientes.maximo-descubierto-default}")
    private BigDecimal maximoDescubiertoDefault;

    public List<Cliente> findAll(String searchTerm) {
        List<Cliente> clientes = clienteRepository.findAll();

        return clientes.stream()
                .filter(c -> searchTerm == null || c.getNombre().toLowerCase().contains(searchTerm.toLowerCase()))
                .filter(c -> searchTerm == null
                        || c.getCorreoElectronico().toLowerCase().contains(searchTerm.toLowerCase()))
                .filter(c -> searchTerm == null || c.getCuit().equals(searchTerm))
                .collect(Collectors.toList());
    }

    public Optional<Cliente> findById(Integer id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> findByEmail(String email) {
        return clienteRepository.findByCorreoElectronico(email);
    }

    public List<Cliente> findByUsuarioId(Integer usuarioId, String searchTerm) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getClientes() != null) {
            List<Cliente> clientes = usuarioOpt.get().getClientes();
            
            // Aplicar los mismos filtros que findAll
            return clientes.stream()
                .filter(c -> searchTerm == null || 
                    c.getNombre().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    c.getCorreoElectronico().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    c.getCuit().contains(searchTerm))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Cliente save(Cliente cliente) {
        if (cliente.getMaximoDescubierto() == null) {
            cliente.setMaximoDescubierto(maximoDescubiertoDefault);
        }
        return clienteRepository.save(cliente);
    }

    public Cliente save(Cliente cliente, Integer usuarioId) {
        if (cliente.getMaximoDescubierto() == null) {
            cliente.setMaximoDescubierto(maximoDescubiertoDefault);
        }
        
        // Guardar el cliente primero
        Cliente clienteGuardado = clienteRepository.save(cliente);
        
        // Asociar el cliente al usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getClientes() == null) {
                usuario.setClientes(new java.util.ArrayList<>());
            }
            usuario.getClientes().add(clienteGuardado);
            usuarioRepository.save(usuario);
            log.info("Cliente {} asociado automáticamente al usuario {}", clienteGuardado.getId(), usuarioId);
        } else {
            log.warn("No se pudo asociar el cliente al usuario {} porque no existe", usuarioId);
        }
        
        return clienteGuardado;
    }

    public Cliente update(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(Integer id) {
        // Buscar el cliente
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            // Verificar si el cliente tiene obras asociadas
            List<Obra> obras = obraRepository.findByCliente(cliente);
            if (obras != null && !obras.isEmpty()) {
                log.warn("No se puede eliminar el cliente {} porque tiene {} obras asociadas", id, obras.size());
                throw new IllegalStateException("No se puede eliminar el cliente porque tiene " + obras.size() + " obras asociadas");
            }
            
            // Desasociar todos los usuarios de este cliente
            List<Usuario> usuarios = cliente.getUsuarios();
            if (usuarios != null && !usuarios.isEmpty()) {
                for (Usuario usuario : usuarios) {
                    usuario.getClientes().remove(cliente);
                    usuarioRepository.save(usuario);
                }
                log.info("Desasociados {} usuarios del cliente {}", usuarios.size(), id);
            }
            
            // Ahora eliminar el cliente
            clienteRepository.deleteById(id);
            log.info("Cliente {} eliminado correctamente", id);
        }
    }

    public boolean verificarSaldoDisponible(Integer clienteId, BigDecimal montoPedido) {
        try {
            // Obtener el cliente
            Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
            if (!clienteOpt.isPresent()) {
                log.error("Cliente no encontrado con ID: {}", clienteId);
                return false;
            }

            Cliente cliente = clienteOpt.get();

            // Obtener el monto total de pedidos no entregados/rechazados
            BigDecimal montoCompromisoPendiente = obtenerMontoCompromisoPendiente(clienteId);

            // Calcular el monto total comprometido (pedidos pendientes + nuevo pedido)
            BigDecimal montoTotalComprometido = montoCompromisoPendiente.add(montoPedido);

            // Verificar si no supera el máximo descubierto
            boolean tieneSaldo = montoTotalComprometido.compareTo(cliente.getMaximoDescubierto()) <= 0;

            log.info(
                    "Cliente {}: Máximo descubierto: {}, Compromiso pendiente: {}, Nuevo pedido: {}, Total comprometido: {}, Tiene saldo: {}",
                    clienteId, cliente.getMaximoDescubierto(), montoCompromisoPendiente,
                    montoPedido, montoTotalComprometido, tieneSaldo);

            return tieneSaldo;

        } catch (Exception e) {
            log.error("Error al verificar saldo del cliente {}: {}", clienteId, e.getMessage());
            return false;
        }
    }

    private BigDecimal obtenerMontoCompromisoPendiente(Integer clienteId) {
        try {
            String url = "http://MS-PEDIDOS/api/pedidos/cliente/" + clienteId + "/monto-pendiente";
            log.info("Consultando monto pendiente en URL: {}", url);

            BigDecimal monto = restTemplate.getForObject(url, BigDecimal.class);
            return monto != null ? monto : BigDecimal.ZERO;

        } catch (HttpClientErrorException e) {
            log.warn("Error al consultar monto pendiente para cliente {}: {}", clienteId, e.getMessage());
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error inesperado al consultar monto pendiente para cliente {}: {}", clienteId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
