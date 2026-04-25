package com.psicomanager.api.alert.model;

import com.psicomanager.api.alert.enums.AlertScope;
import com.psicomanager.api.alert.enums.AlertType;
import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.schedule.model.Schedule;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa um aviso ou lembrete associado a um paciente ou sessão específica.
 * <p>
 * Avisos de escopo {@link AlertScope#PATIENT} persistem até serem descartados manualmente.
 * Avisos de escopo {@link AlertScope#SESSION} são desativados automaticamente quando
 * a sessão vinculada é concluída.
 * </p>
 */
@Entity(name = "alerts")
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Alert {

    // region Identidade

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // endregion

    // region Relacionamentos

    @NotNull
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Sessão vinculada. Preenchido apenas quando {@code scope} é {@link AlertScope#SESSION}.
     */
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = true)
    private Schedule session;

    // endregion

    // region Dados do aviso

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlertType type = AlertType.MANUAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private AlertScope scope;

    @NotNull
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // endregion

    // region Status e datas

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // endregion

    // region Hooks JPA

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // endregion
}
