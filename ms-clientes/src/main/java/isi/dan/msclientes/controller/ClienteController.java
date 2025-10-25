package isi.dan.msclientes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.servicios.ClienteService;
// import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Value("${dan.clientes.instancia}")
    private String instancia;

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    @LogExecutionTime
    public List<Cliente> getAll(@RequestParam(required = false) String searchTerm) {
        return clienteService.findAll(searchTerm);
    }

    @GetMapping("/echo")
    @LogExecutionTime
    public String getEcho() {
        log.debug("Recibiendo un echo ----- {}", instancia);
        return Instant.now() + " - " + instancia;
    }

    @GetMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Cliente> getById(@PathVariable Integer id) throws ClienteNotFoundException {
        Optional<Cliente> cliente = clienteService.findById(id);
        return ResponseEntity
                .ok(cliente.orElseThrow(() -> new ClienteNotFoundException("Cliente " + id + " no encontrado")));
    }

    @PostMapping
    @LogExecutionTime
    public Cliente create(@RequestBody @Validated Cliente cliente) {
        return clienteService.save(cliente);
    }

    @PutMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Cliente> update(@PathVariable final Integer id, @RequestBody Cliente cliente)
            throws ClienteNotFoundException {
        if (!clienteService.findById(id).isPresent()) {
            throw new ClienteNotFoundException("Cliente " + id + " no encontrado");
        }
        cliente.setId(id);
        return ResponseEntity.ok(clienteService.update(cliente));
    }

    @DeleteMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Void> delete(@PathVariable Integer id) throws ClienteNotFoundException {
        if (!clienteService.findById(id).isPresent()) {
            throw new ClienteNotFoundException("Cliente " + id + " no encontrado para borrar");
        }
        clienteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clienteId}/saldo/{monto}")
    public ResponseEntity<Boolean> verificarSaldoCliente(
            @PathVariable Integer clienteId,
            @PathVariable BigDecimal monto) {
        try {
            boolean tieneSaldo = clienteService.verificarSaldoDisponible(clienteId, monto);
            return ResponseEntity.ok(tieneSaldo);
        } catch (Exception e) {
            // En caso de error, devolver false para que el pedido quede en RECIBIDO
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
