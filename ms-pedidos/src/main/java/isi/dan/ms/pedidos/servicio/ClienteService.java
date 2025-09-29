package isi.dan.ms.pedidos.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isi.dan.ms.pedidos.dto.ClienteDTO;
import java.math.BigDecimal;

@Service
public class ClienteService {

  private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

  @Autowired
  private RestTemplate restTemplate;

  @Value("${ms.clientes.url:http://localhost:8081}")
  private String clientesServiceUrl;

  public ClienteDTO obtenerCliente(Long clienteId) {
    try {
      String url = clientesServiceUrl + "/api/clientes/" + clienteId;
      log.info("Consultando cliente en URL: {}", url);

      ClienteDTO cliente = restTemplate.getForObject(url, ClienteDTO.class);
      log.info("Cliente encontrado: {}", cliente != null ? cliente.getId() : "null");

      return cliente;
    } catch (HttpClientErrorException.NotFound e) {
      log.error("Cliente no encontrado con ID: {}", clienteId);
      throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);
    } catch (Exception e) {
      log.error("Error al consultar cliente: {}", e.getMessage());
      throw new RuntimeException("Error al comunicarse con el servicio de clientes", e);
    }
  }

  public boolean verificarSaldoCliente(Long clienteId, BigDecimal montoRequerido) {
    try {
      String url = clientesServiceUrl + "/api/clientes/" + clienteId + "/saldo/" + montoRequerido;
      log.info("Verificando saldo del cliente {} para monto: {}", clienteId, montoRequerido);

      Boolean tieneSaldo = restTemplate.getForObject(url, Boolean.class);
      log.info("Cliente {} tiene saldo suficiente: {}", clienteId, tieneSaldo);

      return tieneSaldo != null ? tieneSaldo : false;
    } catch (HttpClientErrorException.NotFound e) {
      log.error("Cliente no encontrado para verificación de saldo: {}", clienteId);
      return false;
    } catch (Exception e) {
      log.error("Error al verificar saldo del cliente: {}", e.getMessage());
      return false;
    }
  }
}