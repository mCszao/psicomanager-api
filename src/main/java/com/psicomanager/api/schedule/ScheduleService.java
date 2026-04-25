package com.psicomanager.api.schedule;

import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.plan.PlanRepository;
import com.psicomanager.api.plan.PlanService;
import com.psicomanager.api.plan.exception.PlanNotFoundException;
import com.psicomanager.api.schedule.dto.ScheduleAnnotationsDTO;
import com.psicomanager.api.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.schedule.dto.ScheduleRescheduleDTO;
import com.psicomanager.api.schedule.dto.ScheduleResponseDTO;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import com.psicomanager.api.schedule.exception.*;
import com.psicomanager.api.schedule.mapper.ScheduleMapper;
import com.psicomanager.api.schedule.model.Schedule;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia o ciclo de vida das sessões de atendimento.
 * <p>
 * Suporta criação simples (sessão única), criação em lote avulso (via frequência)
 * e integração com planos de atendimento. Toda operação que envolva datas passa
 * pela validação de conflito de horário via {@link #assertNoConflict}.
 * </p>
 */
@Service
@Slf4j
public class ScheduleService {

    // region Dependências

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    @Lazy
    private PlanService planService;

    @Autowired
    private ScheduleMapper mapper;

    // endregion

    // region Utilitários de data e conflito

    /**
     * Resolve a data/hora de fim de uma sessão.
     * Quando {@code end} não é informado, assume 1 hora após o início.
     *
     * @param start início da sessão
     * @param end   fim da sessão, pode ser {@code null}
     * @return data/hora de fim resolvida
     */
    private LocalDateTime resolveEnd(LocalDateTime start, LocalDateTime end) {
        return end != null ? end : start.plusHours(1);
    }

    /**
     * Verifica se existe conflito de horário para o intervalo informado.
     * Lança {@link ScheduleConflictTimeException} caso haja sobreposição com
     * alguma sessão aberta existente.
     *
     * @param start     início do intervalo a verificar
     * @param end       fim do intervalo a verificar
     * @param excludeId ID da sessão a ignorar (usado em reagendamentos); pode ser {@code null}
     */
    private void assertNoConflict(LocalDateTime start, LocalDateTime end, String excludeId) {
        var conflicts = scheduleRepo.findConflictingSchedules(start, end, excludeId);
        if (!conflicts.isEmpty()) throw new ScheduleConflictTimeException();
    }

    /**
     * Avança um {@link LocalDateTime} pelo período correspondente à frequência informada.
     *
     * @param current   data/hora atual
     * @param frequency frequência de repetição
     * @return próxima data/hora na sequência
     */
    private LocalDateTime nextSessionDate(LocalDateTime current, FrequencyEnum frequency) {
        return switch (frequency) {
            case DAILY    -> current.plusDays(1);
            case WEEKLY   -> current.plusWeeks(1);
            case BIWEEKLY -> current.plusWeeks(2);
            case MONTHLY  -> current.plusMonths(1);
        };
    }

    // endregion

    // region Criação

    /**
     * Cria uma ou mais sessões de atendimento.
     * <p>
     * Quando {@code frequency} e {@code sessionsCount} são informados e
     * {@code sessionsCount > 1}, cria um lote de sessões espaçadas pela frequência.
     * Cada sessão é verificada individualmente contra conflitos de horário.
     * Caso contrário, cria uma sessão única.
     * </p>
     *
     * @param dto payload de criação
     * @throws PatientNotFoundException      se o paciente não for encontrado
     * @throws PlanNotFoundException         se o plano informado não for encontrado
     * @throws ScheduleConflictTimeException se qualquer sessão conflitar com outra já existente
     */
    @Transactional
    public void createSchedule(ScheduleRegisterDTO dto) {
        log.info("Buscando informações do paciente de id " + dto.patientId());
        var patient = patientRepo.findById(dto.patientId()).orElseThrow(PatientNotFoundException::new);

        var plan = dto.planId() != null
                ? planRepo.findById(dto.planId()).orElseThrow(PlanNotFoundException::new)
                : null;

        if (dto.frequency() != null && dto.sessionsCount() != null && dto.sessionsCount() > 1) {
            log.info("Criando " + dto.sessionsCount() + " sessões em lote com frequência " + dto.frequency());
            List<Schedule> sessions = new ArrayList<>();
            LocalDateTime current = dto.dateStart();

            for (int i = 0; i < dto.sessionsCount(); i++) {
                LocalDateTime end = resolveEnd(current, i == 0 ? dto.dateEnd() : null);
                assertNoConflict(current, end, null);

                Schedule session = new Schedule();
                session.setPatient(patient);
                session.setPlan(plan);
                session.setDateStart(current);
                session.setDateEnd(end);
                session.setStage(dto.stage() != null ? dto.stage() : StageEnum.OPENED);
                session.setType(dto.type() != null ? dto.type() : AttendanceTypeEnum.PRESENTIAL);
                sessions.add(session);

                current = nextSessionDate(current, dto.frequency());
            }

            scheduleRepo.saveAll(sessions);
            log.info(sessions.size() + " sessões criadas com sucesso");
            return;
        }

        log.info("Verificando conflito de horário para nova consulta");
        assertNoConflict(dto.dateStart(), resolveEnd(dto.dateStart(), dto.dateEnd()), null);
        Schedule formedSchedule = mapper.dtoToEntity(dto, patient);
        formedSchedule.setPlan(plan);
        log.info("Salvando nova consulta do paciente de id " + dto.patientId());
        scheduleRepo.save(formedSchedule);
    }

    // endregion

    // region Consultas

    /**
     * Retorna todas as sessões cadastradas.
     *
     * @return lista de DTOs de sessões
     */
    public List<ScheduleResponseDTO> getAllSchedules() {
        log.info("Buscando por todas as consultas");
        return scheduleRepo.findAll().stream().map(ScheduleMapper::toDto).toList();
    }

    /**
     * Retorna todas as sessões de um paciente.
     *
     * @param patientId ID do paciente
     * @return lista de DTOs de sessões
     * @throws PatientNotFoundException se o paciente não for encontrado
     */
    public List<ScheduleResponseDTO> getAllByPatientId(String patientId) {
        patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
        return scheduleRepo.findByPatientId(patientId).stream().map(ScheduleMapper::toDto).toList();
    }

    /**
     * Retorna uma sessão pelo seu ID.
     *
     * @param id ID da sessão
     * @return DTO de resposta da sessão
     * @throws ScheduleNotFoundException se a sessão não for encontrada
     */
    public ScheduleResponseDTO getScheduleById(String id) {
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        return ScheduleMapper.toDto(schedule);
    }

    // endregion

    // region Mutações de estado

    /**
     * Conclui uma sessão aberta, preenchendo {@code dateEnd} com o momento atual.
     * Quando a sessão pertence a um plano, notifica o {@link PlanService} para
     * atualizar o ciclo de vida do plano.
     *
     * @param id ID da sessão
     * @throws ScheduleNotFoundException        se a sessão não for encontrada
     * @throws ScheduleAlreadyConcludedException se a sessão não estiver aberta
     */
    @Transactional
    public void concludeSession(String id) {
        log.info("Buscando sessão de id " + id + " para conclusão");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyConcludedException();
        schedule.setStage(StageEnum.CONCLUDED);
        schedule.setDateEnd(LocalDateTime.now());
        scheduleRepo.save(schedule);

        if (schedule.getPlan() != null) {
            var plan = schedule.getPlan();
            long concludedCount = scheduleRepo.countByPlanIdAndStage(plan.getId(), StageEnum.CONCLUDED);
            long totalCount = scheduleRepo.countByPlanId(plan.getId());
            planService.onSessionConcluded(plan, concludedCount == 1, concludedCount == totalCount);
        }

        log.info("Sessão de id " + id + " concluída com sucesso");
    }

    /**
     * Cancela uma sessão aberta.
     *
     * @param id ID da sessão
     * @throws ScheduleNotFoundException       se a sessão não for encontrada
     * @throws ScheduleAlreadyCancelledException se a sessão não estiver aberta
     */
    @Transactional
    public void cancelSession(String id) {
        log.info("Buscando sessão de id " + id + " para cancelamento");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyCancelledException();
        schedule.setStage(StageEnum.CANCELLED);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " cancelada com sucesso");
    }

    /**
     * Marca uma sessão aberta como falta do paciente.
     *
     * @param id ID da sessão
     * @throws ScheduleNotFoundException      se a sessão não for encontrada
     * @throws ScheduleAlreadyAbsentException se a sessão não estiver aberta
     */
    @Transactional
    public void markAsAbsent(String id) {
        log.info("Buscando sessão de id " + id + " para marcar falta");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyAbsentException();
        schedule.setStage(StageEnum.ABSENT);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " marcada como falta");
    }

    /**
     * Salva ou atualiza as anotações de uma sessão.
     *
     * @param id  ID da sessão
     * @param dto payload com o texto das anotações (aceita {@code null} para limpar)
     * @throws ScheduleNotFoundException se a sessão não for encontrada
     */
    @Transactional
    public void saveAnnotations(String id, ScheduleAnnotationsDTO dto) {
        log.info("Buscando sessão de id " + id + " para salvar anotações");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        schedule.setAnnotations(dto.annotations());
        scheduleRepo.save(schedule);
        log.info("Anotações da sessão de id " + id + " salvas com sucesso");
    }

    /**
     * Reagenda uma sessão aberta para uma nova data/hora.
     * <p>
     * A sessão original é marcada como {@code RESCHEDULED} e vinculada à nova sessão.
     * O plano da sessão original é herdado pela nova sessão.
     * A nova data é verificada contra conflitos de horário antes da persistência.
     * </p>
     *
     * @param id  ID da sessão a reagendar
     * @param dto payload com a nova data de início e, opcionalmente, de fim
     * @throws ScheduleNotFoundException         se a sessão não for encontrada
     * @throws ScheduleAlreadyRescheduledException se a sessão não estiver aberta
     * @throws ScheduleConflictTimeException      se a nova data conflitar com outra sessão
     */
    @Transactional
    public void rescheduleSession(String id, ScheduleRescheduleDTO dto) {
        log.info("Buscando sessão de id " + id + " para reagendamento");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyRescheduledException();

        LocalDateTime newEnd = resolveEnd(dto.dateStart(), dto.dateEnd());
        assertNoConflict(dto.dateStart(), newEnd, id);

        Schedule newSchedule = new Schedule();
        newSchedule.setPatient(schedule.getPatient());
        newSchedule.setPlan(schedule.getPlan());
        newSchedule.setDateStart(dto.dateStart());
        newSchedule.setDateEnd(newEnd);
        newSchedule.setStage(StageEnum.OPENED);
        newSchedule.setType(schedule.getType());
        scheduleRepo.save(newSchedule);

        schedule.setStage(StageEnum.RESCHEDULED);
        schedule.setRescheduledTo(newSchedule);
        scheduleRepo.save(schedule);

        log.info("Sessão de id " + id + " reagendada com sucesso");
    }

    // endregion
}
