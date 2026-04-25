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

@Service
@Slf4j
public class ScheduleService {

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

    private LocalDateTime resolveEnd(LocalDateTime start, LocalDateTime end) {
        return end != null ? end : start.plusHours(1);
    }

    private void assertNoConflict(LocalDateTime start, LocalDateTime effectiveEnd, String excludeId) {
        var conflicts = scheduleRepo.findConflictingSchedules(start, effectiveEnd, excludeId);
        if (!conflicts.isEmpty()) throw new ScheduleConflictTimeException();
    }

    private LocalDateTime nextSessionDate(LocalDateTime current, FrequencyEnum frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case BIWEEKLY -> current.plusWeeks(2);
            case MONTHLY -> current.plusMonths(1);
        };
    }

    @Transactional
    public void createSchedule(ScheduleRegisterDTO dto) {
        log.info("Buscando informações do paciente de id " + dto.patientId());
        var patient = patientRepo.findById(dto.patientId()).orElseThrow(PatientNotFoundException::new);

        // Resolve plano se informado
        var plan = dto.planId() != null
                ? planRepo.findById(dto.planId()).orElseThrow(PlanNotFoundException::new)
                : null;

        // Criação em lote avulso (sem plano, com frequency + sessionsCount)
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

        // Criação simples — sessão única
        log.info("Verificando conflito de horário para nova consulta");
        assertNoConflict(dto.dateStart(), resolveEnd(dto.dateStart(), dto.dateEnd()), null);
        Schedule formedSchedule = mapper.dtoToEntity(dto, patient);
        formedSchedule.setPlan(plan);
        log.info("Salvando nova consulta do paciente de id " + dto.patientId());
        scheduleRepo.save(formedSchedule);
    }

    public List<ScheduleResponseDTO> getAllSchedules() {
        log.info("Buscando por todas as consultas");
        return scheduleRepo.findAll().stream().map(ScheduleMapper::toDto).toList();
    }

    public List<ScheduleResponseDTO> getAllByPatientId(String patientId) {
        patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
        return scheduleRepo.findByPatientId(patientId).stream().map(ScheduleMapper::toDto).toList();
    }

    public ScheduleResponseDTO getScheduleById(String id) {
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        return ScheduleMapper.toDto(schedule);
    }

    @Transactional
    public void concludeSession(String id) {
        log.info("Buscando sessão de id " + id + " para conclusão");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyConcludedException();
        schedule.setStage(StageEnum.CONCLUDED);
        schedule.setDateEnd(LocalDateTime.now());
        scheduleRepo.save(schedule);

        // Notifica o plano se esta sessão estiver vinculada
        if (schedule.getPlan() != null) {
            var plan = schedule.getPlan();
            long concludedCount = scheduleRepo.countByPlanIdAndStage(plan.getId(), StageEnum.CONCLUDED);
            long totalCount = scheduleRepo.countByPlanId(plan.getId());
            boolean isFirst = concludedCount == 1;
            boolean isLast = concludedCount == totalCount;
            planService.onSessionConcluded(plan, isFirst, isLast);
        }

        log.info("Sessão de id " + id + " concluída com sucesso");
    }

    @Transactional
    public void cancelSession(String id) {
        log.info("Buscando sessão de id " + id + " para cancelamento");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyCancelledException();
        schedule.setStage(StageEnum.CANCELLED);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " cancelada com sucesso");
    }

    @Transactional
    public void markAsAbsent(String id) {
        log.info("Buscando sessão de id " + id + " para marcar falta");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyAbsentException();
        schedule.setStage(StageEnum.ABSENT);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " marcada como falta");
    }

    @Transactional
    public void saveAnnotations(String id, ScheduleAnnotationsDTO dto) {
        log.info("Buscando sessão de id " + id + " para salvar anotações");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        schedule.setAnnotations(dto.annotations());
        scheduleRepo.save(schedule);
        log.info("Anotações da sessão de id " + id + " salvas com sucesso");
    }

    @Transactional
    public void rescheduleSession(String id, ScheduleRescheduleDTO dto) {
        log.info("Buscando sessão de id " + id + " para reagendamento");
        var schedule = scheduleRepo.findById(id).orElseThrow(ScheduleNotFoundException::new);
        if (schedule.getStage() != StageEnum.OPENED) throw new ScheduleAlreadyRescheduledException();
        LocalDateTime newEnd = resolveEnd(dto.dateStart(), dto.dateEnd());
        assertNoConflict(dto.dateStart(), newEnd, id);
        Schedule newSchedule = new Schedule();
        newSchedule.setPatient(schedule.getPatient());
        newSchedule.setPlan(schedule.getPlan()); // herda o plano da sessão original
        newSchedule.setDateStart(dto.dateStart());
        newSchedule.setDateEnd(dto.dateEnd() != null ? dto.dateEnd() : dto.dateStart().plusHours(1));
        newSchedule.setStage(StageEnum.OPENED);
        newSchedule.setType(schedule.getType());
        scheduleRepo.save(newSchedule);
        schedule.setStage(StageEnum.RESCHEDULED);
        schedule.setRescheduledTo(newSchedule);
        scheduleRepo.save(schedule);
        log.info("Sessão de id " + id + " reagendada com sucesso");
    }
}
