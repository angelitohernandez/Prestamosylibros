package mx.edu.utez.Prestamosylibros.repository;

import mx.edu.utez.Prestamosylibros.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}