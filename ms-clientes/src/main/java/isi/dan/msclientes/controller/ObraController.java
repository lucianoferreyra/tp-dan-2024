package isi.dan.msclientes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ObraService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/obras")
public class ObraController {

    @Autowired
    private ObraService obraService;

    @GetMapping
    @LogExecutionTime
    public List<Obra> getAll() {
        return obraService.findAll();
    }

    @GetMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Obra> getById(@PathVariable Integer id) {
        Optional<Obra> obra = obraService.findById(id);
        return obra.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Obra create(@RequestBody Obra obra) {
        return obraService.save(obra);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Obra> update(@PathVariable Integer id, @RequestBody Obra obra) {
        if (!obraService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        obra.setId(id);
        return ResponseEntity.ok(obraService.update(obra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!obraService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        obraService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/finalizar")
    @LogExecutionTime
    public ResponseEntity<Map<String, Object>> finalizarObra(@PathVariable Integer id) {
        try {
            Obra obraFinalizada = obraService.finalizarObra(id);

            Map<String, Object> response = new HashMap<>();
            response.put("obra", obraFinalizada);
            response.put("mensaje", "Obra finalizada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/pendiente")
    @LogExecutionTime
    public ResponseEntity<Map<String, Object>> pasarAPendiente(@PathVariable Integer id) {
        try {
            Obra obraPendiente = obraService.pasarAPendiente(id);

            Map<String, Object> response = new HashMap<>();
            response.put("obra", obraPendiente);
            response.put("mensaje", "Obra pasada a estado pendiente exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/habilitar")
    @LogExecutionTime
    public ResponseEntity<Map<String, Object>> pasarAHabilitada(@PathVariable Integer id) {
        try {
            Obra obraHabilitada = obraService.pasarAHabilitada(id);

            Map<String, Object> response = new HashMap<>();
            response.put("obra", obraHabilitada);
            response.put("mensaje", "Obra habilitada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
