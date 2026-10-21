package mx.edu.utez.Prestamosylibros.controller;

import mx.edu.utez.Prestamosylibros.model.Lector;
import mx.edu.utez.Prestamosylibros.service.LectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lectores")
@RequiredArgsConstructor
public class LectorController {

    private final LectorService lectorService;

    @PostMapping
    public ResponseEntity<Lector> crearLector(@RequestBody Lector lector) {
        return new ResponseEntity<>(lectorService.crearLector(lector), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/rehabilitar")
    public ResponseEntity<Lector> rehabilitarLector(@PathVariable Long id) {
        return ResponseEntity.ok(lectorService.rehabilitarLector(id));
    }
}