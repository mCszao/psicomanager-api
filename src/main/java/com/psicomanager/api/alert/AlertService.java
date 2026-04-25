package com.psicomanager.api.alert;

import com.psicomanager.api.alert.dto.AlertRegisterDTO;
import com.psicomanager.api.alert.dto.AlertResponseDTO;
import com.psicomanager.api.alert.enums.AlertScope;
import com.psicomanager.api.alert.enums.AlertType;
import com.psicomanager.api.alert.exception.AlertNotFoundException;
import com.psicomanager.api.alert.model.Alert;
import com.psicomanager.api.core.exception.BusinessRuleException;
import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.schedule.ScheduleRepository;
import com.psicomanager.api.schedule.exception.ScheduleNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gerencia o ciclo de vida dos avisos de pacientes e sessões.
 * <p>
 * Avisos de escopo {@link AlertScope#SESSION} são desativados automaticamente
 * via {@link #deactivateBySession(String)} ao concluir uma sessão.
 * Avisos de escopo {@link AlertScope#PATIENT} persistem até descarte manual.
 * </p>
 */
@Service
@Slf4j
public class AlertService {

    // region Dependências

    @Autowired
    private AlertRepository alertRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleRepository scheduleRepo;

    // endregion

    // region Mapeamento

    private AlertResponseDTO toDto(Alert alert) {
        return new AlertResponseDTO(
                alert.getId(),
                alert.getPatient().getId(),
                alert.getSession() != null ? alert.getSession().getId() : null,
                alert.getType(),
                alert.getScope(),
                alert.getMessage(),
                alert.getIsActive(),
                alert.getCreatedAt()
        );
    }

    // endregion

    // region Criação

    /**
     * Cria um aviso manual para um paciente ou sessão.
     *
     * @param dto payload de criação
     * @throws PatientNotFoundException  se o paciente não for encontrado
     * @throws ScheduleNotFoundException se o scope for SESSION e a sessão não for encontrada
     * @throws BusinessRuleException     se o scope for SESSION mas sessionId não for informado
     */
    @Transactional
    public void create(AlertRegisterDTO dto) {
        log.info("Criando aviso para o paciente de id " + dto.patientId());

        var patient = patientRepo.findById(dto.patientId())
                .orElseThrow(PatientNotFoundException::new);

        Alert alert = new Alert();
        alert.setPatient(patient);
        alert.setType(AlertType.MANUAL);
        alert.setScope(dto.scope());
        alert.setMessage(dto.message());
        alert.setIsActive(true);

        if (dto.scope() == AlertScope.SESSION) {
            if (dto.sessionId() == null || dto.sessionId().isBlank()) {
                throw new BusinessRuleException(
                        "O ID da sessão é obrigatório para avisos de escopo SESSION.");
            }
            var session = scheduleRepo.findById(dto.sessionId())
                    .orElseThrow(ScheduleNotFoundException::new);
            alert.setSession(session);
        }

        alertRepo.save(alert);
        log.info("Aviso criado com sucesso para o paciente de id " + dto.patientId());
    }

    // endregion

    // region Consultas

    /**
     * Retorna todos os avisos ativos de um paciente (ambos os escopos).
     *
     * @param patientId ID do paciente
     * @return lista de avisos ativos
     */
    public List<AlertResponseDTO> getActiveByPatient(String patientId) {
        log.info("Buscando avisos ativos do paciente de id " + patientId);
        return alertRepo.findByPatientIdAndIsActiveTrue(patientId)
                .stream().map(this::toDto).toList();
    }

    /**
     * Retorna os avisos ativos vinculados a uma sessão específica.
     *
     * @param sessionId ID da sessão
     * @return lista de avisos ativos da sessão
     */
    public List<AlertResponseDTO> getActiveBySession(String sessionId) {
        log.info("Buscando avisos ativos da sessão de id " + sessionId);
        return alertRepo.findBySessionIdAndIsActiveTrue(sessionId)
                .stream().map(this::toDto).toList();
    }

    // endregion

    // region Mutações de estado

    /**
     * Descarta manualmente um aviso, independente do escopo.
     *
     * @param id ID do aviso
     * @throws AlertNotFoundException se o aviso não for encontrado
     */
    @Transactional
    public void dismiss(String id) {
        log.info("Descartando aviso de id " + id);
        var alert = alertRepo.findById(id).orElseThrow(AlertNotFoundException::new);
        alert.setIsActive(false);
        alertRepo.save(alert);
        log.info("Aviso de id " + id + " descartado com sucesso");
    }

    /**
     * Desativa automaticamente todos os avisos de escopo SESSION vinculados a uma sessão.
     * Chamado pelo {@link com.psicomanager.api.schedule.ScheduleService} ao concluir uma sessão.
     *
     * @param sessionId ID da sessão concluída
     */
    @Transactional
    public void deactivateBySession(String sessionId) {
        log.info("Desativando avisos de sess\u00e3o para a sess\u00e3o de id " + sessionId);
        alertRepo.deactivateBySessionId(sessionId);
    }

    // endregion
}
