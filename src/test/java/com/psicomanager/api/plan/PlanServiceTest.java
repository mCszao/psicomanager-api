package com.psicomanager.api.plan;

import com.psicomanager.api.financial.AccountService;
import com.psicomanager.api.financial.FinancialService;
import com.psicomanager.api.infra.tenant.TenantService;
import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.plan.dto.PlanRegisterDTO;
import com.psicomanager.api.plan.exception.PlanNotFoundException;
import com.psicomanager.api.plan.mapper.PlanMapper;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.schedule.ScheduleRepository;
import com.psicomanager.api.user.model.User;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import com.psicomanager.api.schedule.exception.ScheduleConflictTimeException;
import com.psicomanager.api.schedule.model.Schedule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanService")
class PlanServiceTest {

    // region Mocks e setup

    @InjectMocks
    private PlanService service;

    @Mock private PlanRepository planRepo;
    @Mock private PlanTemplateRepository planTemplateRepo;
    @Mock private PatientRepository patientRepo;
    @Mock private ScheduleRepository scheduleRepo;
    @Mock private TenantService tenantService;
    @Mock private FinancialService financialService;
    @Mock private AccountService accountService;

    private static final String PATIENT_ID = "patient-1";
    private static final String PLAN_ID    = "plan-1";
    private static final LocalDate TODAY   = LocalDate.of(2025, 4, 25);

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        // createPlan lê o usuário autenticado do SecurityContext para gerar a
        // cobrança de planos fechados — define um principal de teste.
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(new User(), null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // region Helpers

    private PlanRegisterDTO finiteDto(boolean generateSessions) {
        return new PlanRegisterDTO(
                PATIENT_ID, null, "Plano Teste", null,
                4, FrequencyEnum.WEEKLY, TODAY, null,
                false, generateSessions,
                generateSessions ? "09:00" : null,
                generateSessions ? AttendanceTypeEnum.PRESENTIAL : null
        );
    }

    private PlanRegisterDTO continuousDto() {
        return new PlanRegisterDTO(
                PATIENT_ID, null, "Plano Contínuo", null,
                null, FrequencyEnum.MONTHLY, TODAY, null,
                true, false, null, null
        );
    }

    private Plan activePlan() {
        var plan = new Plan();
        plan.setIsActive(true);
        plan.setIsContinuous(false);
        plan.setSessionsCount(4);
        plan.setFrequency(FrequencyEnum.WEEKLY);
        plan.setPatient(patient);
        return plan;
    }

    // endregion

    // endregion

    // region createPlan

    @Nested
    @DisplayName("createPlan")
    class CreatePlan {

        @Test
        @DisplayName("deve criar plano finito sem geração de sessões")
        void deveCriarPlanoFinitoSemSessoes() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

            assertThatCode(() -> service.createPlan(finiteDto(false)))
                    .doesNotThrowAnyException();

            verify(planRepo).save(any(Plan.class));
            verify(scheduleRepo, never()).saveAll(any());
        }

        @Test
        @DisplayName("deve criar plano e gerar sessões automaticamente quando generateSessions=true")
        void deveCriarPlanoEGerarSessoes() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(any(), any(), any())).thenReturn(List.of());

            service.createPlan(finiteDto(true));

            verify(scheduleRepo).saveAll(argThat(list -> {
                var sessions = (List<?>) list;
                return sessions.size() == 4;
            }));
        }

        @Test
        @DisplayName("deve gerar sessões com o tipo de atendimento correto")
        void deveGerarSessoesComTipoCorreto() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(any(), any(), any())).thenReturn(List.of());

            service.createPlan(finiteDto(true));

            verify(scheduleRepo).saveAll(argThat(list -> {
                @SuppressWarnings("unchecked")
                var sessions = (List<Schedule>) list;
                return sessions.stream().allMatch(s -> s.getType() == AttendanceTypeEnum.PRESENTIAL);
            }));
        }

        @Test
        @DisplayName("deve lançar PatientNotFoundException quando paciente não existe")
        void deveLancarQuandoPacienteNaoExiste() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createPlan(finiteDto(false)))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleConflictTimeException quando sessão gerada conflita")
        void deveLancarQuandoSessaoGeradaConflita() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(any(), any(), any()))
                    .thenReturn(List.of(new Schedule()));

            assertThatThrownBy(() -> service.createPlan(finiteDto(true)))
                    .isInstanceOf(ScheduleConflictTimeException.class);
        }

        @Test
        @DisplayName("deve calcular estimatedEndDate automaticamente quando não informada")
        void deveCalcularEstimatedEndDate() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

            service.createPlan(finiteDto(false));

            // 4 sessões semanais a partir de 25/04/2025 → última em 16/05/2025
            verify(planRepo).save(argThat(p -> {
                Plan plan = (Plan) p;
                return plan.getEstimatedEndDate() != null &&
                       plan.getEstimatedEndDate().equals(TODAY.plusWeeks(3));
            }));
        }

        @Test
        @DisplayName("deve criar plano contínuo sem exigir sessionsCount")
        void deveCriarPlanoContinuo() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

            assertThatCode(() -> service.createPlan(continuousDto()))
                    .doesNotThrowAnyException();

            verify(planRepo).save(any(Plan.class));
        }

        @Test
        @DisplayName("cada sessão gerada deve ter stage OPENED")
        void cadaSessaoDeveSerAberta() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(any(), any(), any())).thenReturn(List.of());

            service.createPlan(finiteDto(true));

            verify(scheduleRepo).saveAll(argThat(list -> {
                @SuppressWarnings("unchecked")
                var sessions = (List<Schedule>) list;
                return sessions.stream().allMatch(s -> s.getStage() == StageEnum.OPENED);
            }));
        }
    }

    // endregion

    // region onSessionConcluded

    @Nested
    @DisplayName("onSessionConcluded")
    class OnSessionConcluded {

        @Test
        @DisplayName("deve preencher startedAt quando é a primeira sessão concluída")
        void devePreencherStartedAtNaPrimeira() {
            var plan = activePlan();

            service.onSessionConcluded(plan, true, false);

            assert plan.getStartedAt() != null;
            verify(planRepo).save(plan);
        }

        @Test
        @DisplayName("não deve sobrescrever startedAt quando já preenchido")
        void naoDeveSobrescreverStartedAt() {
            var plan = activePlan();
            var original = LocalDateTime.of(2025, 1, 1, 0, 0);
            plan.setStartedAt(original);

            service.onSessionConcluded(plan, true, false);

            assert plan.getStartedAt().equals(original);
        }

        @Test
        @DisplayName("deve encerrar plano finito quando última sessão é concluída")
        void deveEncerrarPlanoFinitoNaUltima() {
            var plan = activePlan();

            service.onSessionConcluded(plan, false, true);

            assert !plan.getIsActive();
            assert plan.getEndedAt() != null;
            verify(planRepo).save(plan);
        }

        @Test
        @DisplayName("não deve encerrar plano contínuo mesmo quando isLastSession=true")
        void naoDeveEncerrarPlanoContinuo() {
            var plan = activePlan();
            plan.setIsContinuous(true);

            service.onSessionConcluded(plan, false, true);

            assert Boolean.TRUE.equals(plan.getIsActive());
        }

        @Test
        @DisplayName("não deve alterar plano em sessões intermediárias")
        void naoDeveAlterarEmSessoesIntermediarias() {
            var plan = activePlan();

            service.onSessionConcluded(plan, false, false);

            verify(planRepo, never()).save(any());
        }
    }

    // endregion

    // region deactivatePlan

    @Nested
    @DisplayName("deactivatePlan")
    class DeactivatePlan {

        @Test
        @DisplayName("deve desativar plano e preencher endedAt")
        void deveDesativarPlano() {
            var plan = activePlan();
            when(planRepo.findById(PLAN_ID)).thenReturn(Optional.of(plan));

            service.deactivatePlan(PLAN_ID);

            assert !plan.getIsActive();
            assert plan.getEndedAt() != null;
            verify(planRepo).save(plan);
        }

        @Test
        @DisplayName("deve lançar PlanNotFoundException quando plano não existe")
        void deveLancarQuandoPlanoNaoExiste() {
            when(planRepo.findById(PLAN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deactivatePlan(PLAN_ID))
                    .isInstanceOf(PlanNotFoundException.class);
        }
    }

    // endregion
}
