package com.psicomanager.api.plan;

import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.plan.dto.PlanRegisterDTO;
import com.psicomanager.api.plan.dto.PlanResponseDTO;
import com.psicomanager.api.plan.exception.InvalidPlanConfigException;
import com.psicomanager.api.plan.exception.PlanNotFoundException;
import com.psicomanager.api.plan.exception.PlanTemplateNotFoundException;
import com.psicomanager.api.plan.mapper.PlanMapper;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import com.psicomanager.api.schedule.ScheduleRepository;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import com.psicomanager.api.schedule.exception.ScheduleConflictTimeException;
import com.psicomanager.api.schedule.model.Schedule;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PlanService {

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlanTemplateRepository planTemplateRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleRepository scheduleRepo;

    private LocalDate calculateEstimatedEnd(LocalDate start, FrequencyEnum frequency, int sessionsCount) {
        return switch (frequency) {
            case DAILY -> start.plusDays(sessionsCount - 1L);
            case WEEKLY -> start.plusWeeks(sessionsCount - 1L);
            case BIWEEKLY -> start.plusWeeks((sessionsCount - 1L) * 2);
            case MONTHLY -> start.plusMonths(sessionsCount - 1L);
        };
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
    public void createPlan(PlanRegisterDTO dto) {
        log.info("Buscando paciente de id " + dto.patientId());
        var patient = patientRepo.findById(dto.patientId()).orElseThrow(PatientNotFoundException::new);

        Plan plan = new Plan();
        plan.setPatient(patient);
        plan.setAdherenceDate(dto.adherenceDate());
        plan.setIsActive(true);

        // Herda do template se informado, mas permite sobrescrita
        if (dto.planTemplateId() != null) {
            log.info("Buscando template de plano de id " + dto.planTemplateId());
            PlanTemplate template = planTemplateRepo.findById(dto.planTemplateId())
                    .orElseThrow(PlanTemplateNotFoundException::new);
            plan.setPlanTemplate(template);
            plan.setTitle(dto.title() != null ? dto.title() : template.getTitle());
            plan.setPricePerSession(dto.pricePerSession() != null ? dto.pricePerSession() : template.getPricePerSession());
            plan.setSessionsCount(dto.sessionsCount() != null ? dto.sessionsCount() : template.getSessionsCount());
            plan.setFrequency(dto.frequency() != null ? dto.frequency() : template.getFrequency());
        } else {
            plan.setTitle(dto.title());
            plan.setPricePerSession(dto.pricePerSession());
            plan.setSessionsCount(dto.sessionsCount());
            plan.setFrequency(dto.frequency());
        }

        // Calcula estimatedEndDate se tiver frequency + sessionsCount e não foi informado manualmente
        if (dto.estimatedEndDate() != null) {
            plan.setEstimatedEndDate(dto.estimatedEndDate());
        } else if (plan.getFrequency() != null && plan.getSessionsCount() != null) {
            plan.setEstimatedEndDate(calculateEstimatedEnd(
                    dto.adherenceDate(), plan.getFrequency(), plan.getSessionsCount()));
        }

        planRepo.save(plan);
        log.info("Plano salvo com sucesso para o paciente de id " + dto.patientId());

        // Geração automática de sessões
        if (dto.generateSessions()) {
            if (plan.getFrequency() == null || plan.getSessionsCount() == null) {
                throw new InvalidPlanConfigException(
                        "Para gerar sessões automaticamente é necessário informar frequência e quantidade de sessões");
            }
            if (dto.sessionStartTime() == null || dto.sessionStartTime().isBlank()) {
                throw new InvalidPlanConfigException(
                        "Para gerar sessões automaticamente é necessário informar o horário de início (sessionStartTime)");
            }

            LocalTime startTime = LocalTime.parse(dto.sessionStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime current = dto.adherenceDate().atTime(startTime);
            List<Schedule> sessions = new ArrayList<>();

            for (int i = 0; i < plan.getSessionsCount(); i++) {
                LocalDateTime end = current.plusHours(1);
                var conflicts = scheduleRepo.findConflictingSchedules(current, end, null);
                if (!conflicts.isEmpty()) {
                    throw new ScheduleConflictTimeException();
                }
                Schedule session = new Schedule();
                session.setPatient(patient);
                session.setPlan(plan);
                session.setDateStart(current);
                session.setDateEnd(end);
                session.setStage(StageEnum.OPENED);
                session.setType(AttendanceTypeEnum.PRESENTIAL);
                sessions.add(session);
                current = nextSessionDate(current, plan.getFrequency());
            }

            scheduleRepo.saveAll(sessions);
            log.info(sessions.size() + " sessões geradas automaticamente para o plano " + plan.getId());
        }
    }

    public List<PlanResponseDTO> getAllByPatient(String patientId) {
        patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
        return planRepo.findByPatientId(patientId).stream().map(PlanMapper::toDto).toList();
    }

    public PlanResponseDTO getById(String id) {
        var plan = planRepo.findById(id).orElseThrow(PlanNotFoundException::new);
        return PlanMapper.toDto(plan);
    }

    @Transactional
    public void deactivatePlan(String id) {
        log.info("Desativando plano de id " + id);
        var plan = planRepo.findById(id).orElseThrow(PlanNotFoundException::new);
        plan.setIsActive(false);
        plan.setEndedAt(LocalDateTime.now());
        planRepo.save(plan);
        log.info("Plano de id " + id + " desativado com sucesso");
    }

    // Chamado pelo ScheduleService ao concluir uma sessão vinculada a um plano
    @Transactional
    public void onSessionConcluded(Plan plan, boolean isFirstSession, boolean isLastSession) {
        if (isFirstSession && plan.getStartedAt() == null) {
            plan.setStartedAt(LocalDateTime.now());
            planRepo.save(plan);
        }
        if (isLastSession) {
            plan.setEndedAt(LocalDateTime.now());
            plan.setIsActive(false);
            planRepo.save(plan);
        }
    }
}
