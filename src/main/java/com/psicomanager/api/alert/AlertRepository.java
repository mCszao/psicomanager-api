package com.psicomanager.api.alert;

import com.psicomanager.api.alert.enums.AlertScope;
import com.psicomanager.api.alert.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositório de avisos.
 */
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findByPatientIdAndIsActiveTrueAndScope(String patientId, AlertScope scope);

    List<Alert> findByPatientIdAndIsActiveTrue(String patientId);

    List<Alert> findBySessionIdAndIsActiveTrue(String sessionId);

    /**
     * Desativa todos os avisos de escopo SESSION vinculados a uma sessão específica.
     * Chamado automaticamente ao concluir uma sessão.
     */
    @Modifying
    @Query("UPDATE alerts a SET a.isActive = false WHERE a.session.id = :sessionId AND a.isActive = true")
    void deactivateBySessionId(String sessionId);
}
