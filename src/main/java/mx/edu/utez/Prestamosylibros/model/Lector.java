package mx.edu.utez.Prestamosylibros.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lectores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean membresiaPremium = false;
}