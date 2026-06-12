package com.psicomanager.api.plan;

import com.psicomanager.api.financial.AccountService;
import com.psicomanager.api.financial.FinancialService;
import com.psicomanager.api.infra.tenant.TenantService;
import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.plan.dto.PlanRegisterDTO;
import com.psicomanager.api.plan.dto.PlanResponseDTO;
import com.psicomanager.api.plan.exception.InvalidPlanConfigException;
import com.psicomanager.api.plan.exception.PlanNotFoundException;
import com.psicomanager.api.plan.exception.PlanTemplateNotFoundException;
import com.psicomanager.api.plan.validation.PlanValidator;
import com.psicomanager.api.plan.mapper.PlanMapper;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.plan.template.model.PlanTemplate;
import com.psicomanager.api.schedule.ScheduleRepository;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import com.psicomanager.api.schedule.exception.ScheduleConflictTimeException;
import com.psicomanager.api.schedule.model.Schedule;
import com.psicomanager.api.user.model.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia o ciclo de vida dos planos de atendimento.
 * <p>
 * Responsável por criar planos com ou sem template, calcular datas estimadas,
 * gerar sessões automaticamente e atualizar o status do plano conforme as
 * sessões vinculadas são concluídas.
 * </p>
 */
@Service
@Slf4j
public class PlanService {

    // region Dependências

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlanTemplateRepository planTemplateRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    @Lazy
    private FinancialService financialService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TenantService tenantService;

    // endregion

    // region Utilitários de data e conflito

    /**
     * Calcula a data estimada de encerramento com base na data de início,
     * frequência e quantidade de sessões.
     *
     * @param start         data de início (geralmente a data de adesão)
     * @param frequency     frequência das sessões
     * @param sessionsCount quantidade total de sessões
     * @return data estimada de encerramento
     */
    private LocalDate calculateEstimatedEnd(LocalDate start, FrequencyEnum frequency, int sessionsCount) {
        return switch (frequency) {
            case DAILY    -> start.plusDays(sessionsCount - 1L);
            case WEEKLY   -> start.plusWeeks(sessionsCount - 1L);
            case BIWEEKLY -> start.plusWeeks((sessionsCount - 1L) * 2);
            case MONTHLY  -> start.plusMonths(sessionsCount - 1L);
        };
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

    /**
     * Verifica se existe conflito de horário para o intervalo informado.
     * Lança {@link ScheduleConflictTimeException} caso haja sobreposição com
     * alguma sessão aberta existente.
     *
     * @param start     início do intervalo a verificar
     * @param end       fim do intervalo a verificar
     * @param excludeId ID da sessão a ignorar na verificação (útil em reagendamentos); pode ser {@code null}
     */
    private void assertNoConflict(LocalDateTime start, LocalDateTime end, String excludeId) {
        var conflicts = scheduleRepo.findConflictingSchedules(start, end, excludeId);
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictTimeException();
        }
    }

    // endregion

    // region Criação de plano

    /**
     * Cria um novo plano de atendimento para o paciente informado.
     * <p>
     * Quando {@code planTemplateId} é fornecido, os campos não informados no
     * payload são herdados do template. Quando {@code generateSessions} é
     * {@code true}, as sessões são geradas automaticamente com base em
     * {@code sessionsCount} — obrigatório para qualquer tipo de plano quando
     * a geração automática está ativada.
     * </p>
     * <p>
     * Planos fechados ({@code isContinuous = false}) geram automaticamente uma
     * cobrança do tipo {@code PLAN_CHARGE} após a persistência.
     * </p>
     *
     * @param dto payload de criação do plano
     * @throws PatientNotFoundException      se o paciente não for encontrado
     * @throws PlanTemplateNotFoundException se o template informado não for encontrado
     * @throws InvalidPlanConfigException    se {@code generateSessions} for {@code true}
     *                                        mas {@code sessionsCount}, {@code sessionStartTime}
     *                                        ou {@code attendanceType} não forem informados
     * @throws ScheduleConflictTimeException se alguma sessão gerada conflitar com outra já existente
     */
    @Transactional
    public void createPlan(PlanRegisterDTO dto) {
        log.info("Validando regras de negócio do plano");
        PlanValidator.validateRegister(dto);

        log.info("Buscando paciente de id " + dto.patientId());
        var patient = patientRepo.findById(dto.patientId()).orElseThrow(PatientNotFoundException::new);

        Plan plan = new Plan();
        plan.setPatient(patient);
        plan.setAdherenceDate(dto.adherenceDate());
        plan.setIsActive(true);
        plan.setIsContinuous(dto.isContinuous());
        plan.setOrganizationId(tenantService.required());

        applyTemplateOrDirectValues(plan, dto);
        applyEstimatedEndDate(plan, dto);

        planRepo.save(plan);
        log.info("Plano salvo com sucesso para o paciente de id " + dto.patientId());

        if (dto.generateSessions()) {
            generateSessions(plan, dto);
        }

        // Hook financeiro: planos fechados geram cobrança global imediata
        if (!Boolean.TRUE.equals(plan.getIsContinuous())) {
            log.info("Plano fechado criado. Gerando cobrança financeira para o plano de id " + plan.getId());
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var psychAccount = accountService.getPsychologistAccount(user.getId());
            financialService.generatePlanCharge(plan, psychAccount);
        }
    }

    /**
     * Aplica os valores do template ao plano, respeitando sobrescritas informadas no DTO.
     * Quando nenhum template é informado, aplica os valores diretos do DTO.
     *
     * @param plan plano sendo construído
     * @param dto  payload de criação
     */
    private void applyTemplateOrDirectValues(Plan plan, PlanRegisterDTO dto) {
        if (dto.planTemplateId() != null) {
            log.info("Buscando template de plano de id " + dto.planTemplateId());
            PlanTemplate template = planTemplateRepo.findById(dto.planTemplateId())
                    .orElseThrow(PlanTemplateNotFoundException::new);
            plan.setPlanTemplate(template);
            plan.setTitle(dto.title() != null ? dto.title() : template.getTitle());
            plan.setPricePerSession(dto.pricePerSession() != null ? dto.pricePerSession() : template.getPricePerSession());
            plan.setSessionsCount(dto.sessionsCount() != null ? dto.sessionsCount() : template.getSessionsCount());
            plan.setFrequency(dto.frequency() != null ? dto.frequency() : template.getFrequency());
            plan.setAttendanceType(dto.attendanceType() != null ? dto.attendanceType() : template.getAttendanceType());
        } else {
            plan.setTitle(dto.title());
            plan.setPricePerSession(dto.pricePerSession());
            plan.setSessionsCount(dto.sessionsCount());
            plan.setFrequency(dto.frequency());
            plan.setAttendanceType(dto.attendanceType());
        }
    }

    /**
     * Define a data estimada de encerramento do plano.
     * Usa o valor do DTO quando informado; caso contrário, calcula automaticamente
     * a partir de {@code frequency} e {@code sessionsCount}.
     *
     * @param plan plano sendo construído
     * @param dto  payload de criação
     */
    private void applyEstimatedEndDate(Plan plan, PlanRegisterDTO dto) {
        if (dto.estimatedEndDate() != null) {
            plan.setEstimatedEndDate(dto.estimatedEndDate());
        } else if (plan.getFrequency() != null && plan.getSessionsCount() != null) {
            plan.setEstimatedEndDate(
                    calculateEstimatedEnd(dto.adherenceDate(), plan.getFrequency(), plan.getSessionsCount()));
        }
    }

    /**
     * Gera as sessões automaticamente para o plano recém-criado.
     * <p>
     * A quantidade de sessões é sempre derivada de {@code sessionsCount} — não existe
     * geração automática baseada em períodos de tempo. Cada sessão é verificada
     * individualmente contra conflitos de horário antes de ser adicionada ao lote.
     * </p>
     *
     * @param plan plano já persistido ao qual as sessões serão vinculadas
     * @param dto  payload original com {@code sessionStartTime} e demais configurações
     * @throws InvalidPlanConfigException    se {@code sessionsCount} não estiver disponível
     * @throws ScheduleConflictTimeException se qualquer sessão conflitar com outra já existente
     */
    private void generateSessions(Plan plan, PlanRegisterDTO dto) {
        int count = plan.getSessionsCount() != null ? plan.getSessionsCount() : 0;

        if (count <= 0) {
            throw new InvalidPlanConfigException(
                    "Para gerar sessões automaticamente é necessário informar o número de sessões.");
        }

        AttendanceTypeEnum type = plan.getAttendanceType();
        LocalTime startTime = LocalTime.parse(dto.sessionStartTime());
        LocalDateTime current = dto.adherenceDate().atTime(startTime);
        List<Schedule> sessions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            LocalDateTime end = current.plusHours(1);
            assertNoConflict(current, end, null);

            Schedule session = new Schedule();
            session.setPatient(plan.getPatient());
            session.setPlan(plan);
            session.setDateStart(current);
            session.setDateEnd(end);
            session.setStage(StageEnum.OPENED);
            session.setType(type);
            // Herda o tenant do plano — sem isso a sessão gerada some do calendário
            // (getAllSchedules filtra por organizationId).
            session.setOrganizationId(plan.getOrganizationId());
            if (plan.getPricePerSession() != null) {
                session.setSessionValue(plan.getPricePerSession());
            }
            sessions.add(session);

            current = nextSessionDate(current, plan.getFrequency());
        }

        scheduleRepo.saveAll(sessions);
        log.info(sessions.size() + " sessões geradas automaticamente para o plano " + plan.getId()
                + " com tipo de atendimento " + type);
    }

    // endregion

    // region Consultas

    /**
     * Retorna todos os planos de um paciente.
     *
     * @param patientId ID do paciente
     * @return lista de DTOs de planos
     * @throws PatientNotFoundException se o paciente não for encontrado
     */
    public List<PlanResponseDTO> getAllByPatient(String patientId) {
        patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
        return planRepo.findByPatientId(patientId).stream().map(PlanMapper::toDto).toList();
    }

    /**
     * Retorna um plano pelo seu ID.
     *
     * @param id ID do plano
     * @return DTO de resposta do plano
     * @throws PlanNotFoundException se o plano não for encontrado
     */
    public PlanResponseDTO getById(String id) {
        var plan = planRepo.findById(id).orElseThrow(PlanNotFoundException::new);
        return PlanMapper.toDto(plan);
    }

    // endregion

    // region Mutações de estado

    /**
     * Desativa um plano manualmente, preenchendo {@code endedAt} com o momento atual.
     *
     * @param id ID do plano a desativar
     * @throws PlanNotFoundException se o plano não for encontrado
     */
    @Transactional
    public void deactivatePlan(String id) {
        log.info("Desativando plano de id " + id);
        var plan = planRepo.findById(id).orElseThrow(PlanNotFoundException::new);
        plan.setIsActive(false);
        plan.setEndedAt(LocalDateTime.now());
        planRepo.save(plan);
        log.info("Plano de id " + id + " desativado com sucesso");
    }

    /**
     * Atualiza o ciclo de vida do plano quando uma sessão vinculada é concluída.
     * <p>
     * Deve ser chamado pelo {@link com.psicomanager.api.schedule.ScheduleService}
     * após persistir a conclusão de uma sessão.
     * </p>
     *
     * @param plan           plano ao qual a sessão pertence
     * @param isFirstSession {@code true} se esta é a primeira sessão concluída do plano
     * @param isLastSession  {@code true} se todas as sessões do plano foram concluídas
     */
    @Transactional
    public void onSessionConcluded(Plan plan, boolean isFirstSession, boolean isLastSession) {
        if (isFirstSession && plan.getStartedAt() == null) {
            plan.setStartedAt(LocalDateTime.now());
            planRepo.save(plan);
        }
        // Planos contínuos não encerram automaticamente
        if (isLastSession && !Boolean.TRUE.equals(plan.getIsContinuous())) {
            plan.setEndedAt(LocalDateTime.now());
            plan.setIsActive(false);
            planRepo.save(plan);
        }
    }

    // endregion
}
