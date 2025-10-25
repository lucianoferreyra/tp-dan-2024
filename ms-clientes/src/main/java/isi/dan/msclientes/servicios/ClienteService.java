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
import isi.dan.msclientes.model.Cliente;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

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

    public Cliente save(Cliente cliente) {
        if (cliente.getMaximoDescubierto() == null) {
            cliente.setMaximoDescubierto(maximoDescubiertoDefault);
        }
        return clienteRepository.save(cliente);
    }

    public Cliente update(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(Integer id) {
        clienteRepository.deleteById(id);
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
