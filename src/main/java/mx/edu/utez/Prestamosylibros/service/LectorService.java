package mx.edu.utez.Prestamosylibros.service;

import mx.edu.utez.Prestamosylibros.exception.ResourceNotFoundException;
import mx.edu.utez.Prestamosylibros.model.Lector;
import mx.edu.utez.Prestamosylibros.repository.LectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectorService {

    private final LectorRepository lectorRepository;

    public Lector crearLector(Lector lector) {
        return lectorRepository.save(lector);
    }

    // ⭐ 4. Rehabilitar Lector (10 pts)
    public Lector rehabilitarLector(Long id) {
        Lector lector = lectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lector no encontrado con ID: " + id));

        lector.setActivo(true);
        return lectorRepository.save(lector);
    }
}