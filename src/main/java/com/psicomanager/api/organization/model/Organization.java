package com.psicomanager.api.organization.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa uma organização (clínica ou consultório) no sistema.
 *
 * <p>Toda entidade de dados (paciente, sessão, plano, etc.) pertence a uma organização.
 * Isso garante o isolamento completo de dados entre tenants distintos.</p>
 */
@Entity(name = "organizations")
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    /** Identificador URL-friendly único da organização. Ex: "clinica-saude-mental" */
    @Column(name = "SLUG", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
