package mx.edu.utez.Prestamosylibros.repository;

import mx.edu.utez.Prestamosylibros.model.EstadoPrestamo;
import mx.edu.utez.Prestamosylibros.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    long countByLectorIdAndEstadoIn(Long lectorId, List<EstadoPrestamo> estados);

    List<Prestamo> findByLectorIdAndEstadoIn(Long lectorId, List<EstadoPrestamo> estados);
}