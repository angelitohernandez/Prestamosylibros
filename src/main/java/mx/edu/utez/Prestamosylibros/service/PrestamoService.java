package mx.edu.utez.Prestamosylibros.service;

import mx.edu.utez.Prestamosylibros.dto.PrestamoResponseDTO;
import mx.edu.utez.Prestamosylibros.dto.SolicitudPrestamoDTO;
import mx.edu.utez.Prestamosylibros.exception.BadRequestException;
import mx.edu.utez.Prestamosylibros.exception.ResourceNotFoundException;
import mx.edu.utez.Prestamosylibros.model.EstadoPrestamo;
import mx.edu.utez.Prestamosylibros.model.Lector;
import mx.edu.utez.Prestamosylibros.model.Libro;
import mx.edu.utez.Prestamosylibros.model.Prestamo;
import mx.edu.utez.Prestamosylibros.repository.LectorRepository;
import mx.edu.utez.Prestamosylibros.repository.LibroRepository;
import mx.edu.utez.Prestamosylibros.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LectorRepository lectorRepository;
    private final LibroRepository libroRepository;

    @Transactional
    public PrestamoResponseDTO solicitarPrestamo(SolicitudPrestamoDTO dto) {
        Lector lector = lectorRepository.findById(dto.getLectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Lector no encontrado con ID: " + dto.getLectorId()));

        if (!Boolean.TRUE.equals(lector.getActivo())) {
            throw new BadRequestException("El lector no se encuentra activo");
        }

        Libro libro = libroRepository.findById(dto.getLibroId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + dto.getLibroId()));

        if (libro.getEjemplaresDisponibles() <= 0) {
            throw new BadRequestException("No hay ejemplares disponibles para el libro solicitado");
        }

        List<EstadoPrestamo> estadosActivos = Arrays.asList(EstadoPrestamo.PRESTADO, EstadoPrestamo.VENCIDO);
        long prestamosActivos = prestamoRepository.countByLectorIdAndEstadoIn(lector.getId(), estadosActivos);
        int limitePermitido = Boolean.TRUE.equals(lector.getMembresiaPremium()) ? 5 : 3;

        if (prestamosActivos >= limitePermitido) {
            throw new BadRequestException("El lector ha alcanzado el límite máximo de préstamos activos (" + limitePermitido + ")");
        }

        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1);
        libroRepository.save(libro);

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo();
        prestamo.setLector(lector);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaDevolucionMaxima(hoy.plusDays(14));
        prestamo.setEstado(EstadoPrestamo.PRESTADO);

        Prestamo guardado = prestamoRepository.save(prestamo);
        return mapToDTO(guardado);
    }

    @Transactional
    public PrestamoResponseDTO devolverLibro(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BadRequestException("El préstamo ya se encuentra en estado DEVUELTO");
        }

        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(prestamo.getFechaDevolucionMaxima())) {
            Lector lector = prestamo.getLector();
            lector.setActivo(false);
            lectorRepository.save(lector);
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        Libro libro = prestamo.getLibro();
        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() + 1);
        libroRepository.save(libro);

        Prestamo actualizado = prestamoRepository.save(prestamo);
        return mapToDTO(actualizado);
    }

    @Transactional
    public PrestamoResponseDTO renovarPrestamo(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.VENCIDO || prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BadRequestException("No se puede renovar un préstamo en estado " + prestamo.getEstado());
        }

        prestamo.setFechaDevolucionMaxima(prestamo.getFechaDevolucionMaxima().plusDays(7));
        Prestamo actualizado = prestamoRepository.save(prestamo);
        return mapToDTO(actualizado);
    }

    public List<PrestamoResponseDTO> listarPrestamosActivosLector(Long lectorId) {
        if (!lectorRepository.existsById(lectorId)) {
            throw new ResourceNotFoundException("Lector no encontrado con ID: " + lectorId);
        }

        List<EstadoPrestamo> estadosActivos = Arrays.asList(EstadoPrestamo.PRESTADO, EstadoPrestamo.VENCIDO);
        return prestamoRepository.findByLectorIdAndEstadoIn(lectorId, estadosActivos).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PrestamoResponseDTO mapToDTO(Prestamo prestamo) {
        return PrestamoResponseDTO.builder()
                .id(prestamo.getId())
                .lectorId(prestamo.getLector().getId())
                .nombreLector(prestamo.getLector().getNombre())
                .libroId(prestamo.getLibro().getId())
                .tituloLibro(prestamo.getLibro().getTitulo())
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaDevolucionMaxima(prestamo.getFechaDevolucionMaxima())
                .estado(prestamo.getEstado())
                .build();
    }
}