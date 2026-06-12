package com.psicomanager.api.schedule;

import com.psicomanager.api.alert.AlertService;
import com.psicomanager.api.financial.AccountService;
import com.psicomanager.api.financial.FinancialService;
import com.psicomanager.api.infra.tenant.TenantService;
import com.psicomanager.api.patient.PatientRepository;
import com.psicomanager.api.patient.exception.PatientNotFoundException;
import com.psicomanager.api.patient.model.Patient;
import com.psicomanager.api.plan.PlanRepository;
import com.psicomanager.api.plan.PlanService;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.user.model.User;
import com.psicomanager.api.schedule.dto.ScheduleAnnotationsDTO;
import com.psicomanager.api.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.schedule.dto.ScheduleRescheduleDTO;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.StageEnum;
import com.psicomanager.api.schedule.exception.*;
import com.psicomanager.api.schedule.mapper.ScheduleMapper;
import com.psicomanager.api.schedule.model.Schedule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService")
class ScheduleServiceTest {

    // region Mocks e setup

    @InjectMocks
    private ScheduleService service;

    @Mock private ScheduleRepository scheduleRepo;
    @Mock private PatientRepository patientRepo;
    @Mock private PlanRepository planRepo;
    @Mock private PlanService planService;
    @Mock private AlertService alertService;
    @Mock private ScheduleMapper mapper;
    @Mock private FinancialService financialService;
    @Mock private AccountService accountService;
    @Mock private TenantService tenantService;

    private static final String SCHEDULE_ID = "schedule-1";
    private static final String PATIENT_ID  = "patient-1";
    private static final LocalDateTime START = LocalDateTime.of(2025, 4, 25, 14, 0);
    private static final LocalDateTime END   = START.plusHours(1);

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        // concludeSession lê o usuário autenticado para gerar a cobrança de sessão.
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(new User(), null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // region Helpers

    private Schedule openedSchedule() {
        Schedule s = new Schedule();
        s.setStage(StageEnum.OPENED);
        s.setPatient(patient);
        s.setDateStart(START);
        s.setDateEnd(END);
        return s;
    }

    private Schedule scheduleWithStage(StageEnum stage) {
        Schedule s = openedSchedule();
        s.setStage(stage);
        return s;
    }

    private ScheduleRegisterDTO simpleDto() {
        return new ScheduleRegisterDTO(PATIENT_ID, START, END, null, null, null, null, null, null);
    }

    // endregion

    // endregion

    // region createSchedule

    @Nested
    @DisplayName("createSchedule")
    class CreateSchedule {

        @Test
        @DisplayName("deve criar sessão simples sem conflito")
        void deveCriarSessaoSimplesSemConflito() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(any(), any(), any())).thenReturn(List.of());
            when(mapper.dtoToEntity(any(), any())).thenReturn(openedSchedule());

            assertThatCode(() -> service.createSchedule(simpleDto()))
                    .doesNotThrowAnyException();

            verify(scheduleRepo).save(any(Schedule.class));
        }

        @Test
        @DisplayName("deve lançar PatientNotFoundException quando paciente não existe")
        void deveLancarQuandoPacienteNaoExiste() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createSchedule(simpleDto()))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleConflictTimeException quando há conflito de horário")
        void deveLancarQuandoHaConflito() {
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(any(), any(), any())).thenReturn(List.of(openedSchedule()));

            assertThatThrownBy(() -> service.createSchedule(simpleDto()))
                    .isInstanceOf(ScheduleConflictTimeException.class);
        }

        @Test
        @DisplayName("deve resolver dateEnd como start + 1h quando dateEnd é null")
        void deveResolverDateEndQuandoNulo() {
            var dtoSemFim = new ScheduleRegisterDTO(PATIENT_ID, START, null, null, null, null, null, null, null);
            when(patientRepo.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
            when(scheduleRepo.findConflictingSchedules(eq(START), eq(START.plusHours(1)), isNull()))
                    .thenReturn(List.of());
            when(mapper.dtoToEntity(any(), any())).thenReturn(openedSchedule());

            assertThatCode(() -> service.createSchedule(dtoSemFim))
                    .doesNotThrowAnyException();

            verify(scheduleRepo).findConflictingSchedules(START, START.plusHours(1), null);
        }
    }

    // endregion

    // region concludeSession

    @Nested
    @DisplayName("concludeSession")
    class ConcludeSession {

        @Test
        @DisplayName("deve concluir sessão aberta e salvar")
        void deveConcluirSessaoAberta() {
            var schedule = openedSchedule();
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            lenient().when(scheduleRepo.countByPlanIdAndStage(any(), any())).thenReturn(0L);
            lenient().when(scheduleRepo.countByPlanId(any())).thenReturn(0L);

            service.concludeSession(SCHEDULE_ID);

            assertThat(schedule.getStage()).isEqualTo(StageEnum.CONCLUDED);
            verify(scheduleRepo).save(schedule);
            verify(alertService).deactivateBySession(SCHEDULE_ID);
        }

        @Test
        @DisplayName("deve lançar ScheduleNotFoundException quando sessão não existe")
        void deveLancarQuandoNaoEncontrada() {
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.concludeSession(SCHEDULE_ID))
                    .isInstanceOf(ScheduleNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleAlreadyConcludedException quando sessão não está aberta")
        void deveLancarQuandoSessaoJaConcluida() {
            when(scheduleRepo.findById(SCHEDULE_ID))
                    .thenReturn(Optional.of(scheduleWithStage(StageEnum.CONCLUDED)));

            assertThatThrownBy(() -> service.concludeSession(SCHEDULE_ID))
                    .isInstanceOf(ScheduleAlreadyConcludedException.class);
        }

        @Test
        @DisplayName("deve lançar quando sessão está CANCELLED")
        void deveLancarQuandoSessaoCancelada() {
            when(scheduleRepo.findById(SCHEDULE_ID))
                    .thenReturn(Optional.of(scheduleWithStage(StageEnum.CANCELLED)));

            assertThatThrownBy(() -> service.concludeSession(SCHEDULE_ID))
                    .isInstanceOf(ScheduleAlreadyConcludedException.class);
        }

        @Test
        @DisplayName("deve notificar PlanService quando sessão pertence a um plano")
        void deveNotificarPlanServiceQuandoTemPlano() {
            var schedule = openedSchedule();
            var plan = new Plan();
            plan.setIsContinuous(true);
            schedule.setPlan(plan);

            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleRepo.countByPlanIdAndStage(any(), eq(StageEnum.CONCLUDED))).thenReturn(1L);
            when(scheduleRepo.countByPlanId(any())).thenReturn(5L);

            service.concludeSession(SCHEDULE_ID);

            verify(planService).onSessionConcluded(plan, true, false);
        }

        @Test
        @DisplayName("deve indicar isLastSession=true quando concluded == total")
        void deveIndicarIsLastSessionQuandoUltima() {
            var schedule = openedSchedule();
            var plan = new Plan();
            plan.setIsContinuous(false);
            schedule.setPlan(plan);

            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
            when(scheduleRepo.countByPlanIdAndStage(any(), eq(StageEnum.CONCLUDED))).thenReturn(5L);
            when(scheduleRepo.countByPlanId(any())).thenReturn(5L);

            service.concludeSession(SCHEDULE_ID);

            verify(planService).onSessionConcluded(plan, false, true);
        }
    }

    // endregion

    // region cancelSession

    @Nested
    @DisplayName("cancelSession")
    class CancelSession {

        @Test
        @DisplayName("deve cancelar sessão aberta")
        void deveCancelarSessaoAberta() {
            var schedule = openedSchedule();
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

            service.cancelSession(SCHEDULE_ID);

            assertThat(schedule.getStage()).isEqualTo(StageEnum.CANCELLED);
            verify(scheduleRepo).save(schedule);
        }

        @Test
        @DisplayName("deve lançar ScheduleNotFoundException quando sessão não existe")
        void deveLancarQuandoNaoEncontrada() {
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelSession(SCHEDULE_ID))
                    .isInstanceOf(ScheduleNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleAlreadyCancelledException quando sessão não está aberta")
        void deveLancarQuandoJaCancelada() {
            when(scheduleRepo.findById(SCHEDULE_ID))
                    .thenReturn(Optional.of(scheduleWithStage(StageEnum.CANCELLED)));

            assertThatThrownBy(() -> service.cancelSession(SCHEDULE_ID))
                    .isInstanceOf(ScheduleAlreadyCancelledException.class);
        }

        @Test
        @DisplayName("deve lançar quando sessão está CONCLUDED")
        void deveLancarQuandoSessaoConcluida() {
            when(scheduleRepo.findById(SCHEDULE_ID))
                    .thenReturn(Optional.of(scheduleWithStage(StageEnum.CONCLUDED)));

            assertThatThrownBy(() -> service.cancelSession(SCHEDULE_ID))
                    .isInstanceOf(ScheduleAlreadyCancelledException.class);
        }
    }

    // endregion

    // region markAsAbsent

    @Nested
    @DisplayName("markAsAbsent")
    class MarkAsAbsent {

        @Test
        @DisplayName("deve marcar sessão aberta como falta")
        void deveMarcarFaltaSessaoAberta() {
            var schedule = openedSchedule();
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

            service.markAsAbsent(SCHEDULE_ID);

            assertThat(schedule.getStage()).isEqualTo(StageEnum.ABSENT);
            verify(scheduleRepo).save(schedule);
        }

        @Test
        @DisplayName("deve lançar ScheduleNotFoundException quando sessão não existe")
        void deveLancarQuandoNaoEncontrada() {
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.markAsAbsent(SCHEDULE_ID))
                    .isInstanceOf(ScheduleNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleAlreadyAbsentException quando sessão não está aberta")
        void deveLancarQuandoJaMarcadaComoFalta() {
            when(scheduleRepo.findById(SCHEDULE_ID))
                    .thenReturn(Optional.of(scheduleWithStage(StageEnum.ABSENT)));

            assertThatThrownBy(() -> service.markAsAbsent(SCHEDULE_ID))
                    .isInstanceOf(ScheduleAlreadyAbsentException.class);
        }
    }

    // endregion

    // region saveAnnotations

    @Nested
    @DisplayName("saveAnnotations")
    class SaveAnnotations {

        @Test
        @DisplayName("deve salvar anotações na sessão")
        void deveSalvarAnotacoes() {
            var schedule = openedSchedule();
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

            service.saveAnnotations(SCHEDULE_ID, new ScheduleAnnotationsDTO("Anotação de teste"));

            assertThat(schedule.getAnnotations()).isEqualTo("Anotação de teste");
            verify(scheduleRepo).save(schedule);
        }

        @Test
        @DisplayName("deve aceitar anotações null para limpar o campo")
        void deveAceitarAnotacoesNulas() {
            var schedule = openedSchedule();
            schedule.setAnnotations("Texto anterior");
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

            service.saveAnnotations(SCHEDULE_ID, new ScheduleAnnotationsDTO(null));

            assertThat(schedule.getAnnotations()).isNull();
            verify(scheduleRepo).save(schedule);
        }

        @Test
        @DisplayName("deve lançar ScheduleNotFoundException quando sessão não existe")
        void deveLancarQuandoNaoEncontrada() {
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.saveAnnotations(SCHEDULE_ID, new ScheduleAnnotationsDTO("x")))
                    .isInstanceOf(ScheduleNotFoundException.class);
        }
    }

    // endregion

    // region rescheduleSession

    @Nested
    @DisplayName("rescheduleSession")
    class RescheduleSession {

        @Test
        @DisplayName("deve reagendar sessão aberta criando nova e marcando original como RESCHEDULED")
        void deveReagendarSessaoAberta() {
            var original = openedSchedule();
            original.setType(AttendanceTypeEnum.PRESENTIAL);
            var newStart = START.plusDays(1);
            var dto = new ScheduleRescheduleDTO(newStart, newStart.plusHours(1));

            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(original));
            when(scheduleRepo.findConflictingSchedules(any(), any(), eq(SCHEDULE_ID))).thenReturn(List.of());

            service.rescheduleSession(SCHEDULE_ID, dto);

            assertThat(original.getStage()).isEqualTo(StageEnum.RESCHEDULED);
            verify(scheduleRepo, times(2)).save(any(Schedule.class));
        }

        @Test
        @DisplayName("deve lançar ScheduleNotFoundException quando sessão não existe")
        void deveLancarQuandoNaoEncontrada() {
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.rescheduleSession(SCHEDULE_ID,
                    new ScheduleRescheduleDTO(START, END)))
                    .isInstanceOf(ScheduleNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleAlreadyRescheduledException quando sessão não está aberta")
        void deveLancarQuandoJaReagendada() {
            when(scheduleRepo.findById(SCHEDULE_ID))
                    .thenReturn(Optional.of(scheduleWithStage(StageEnum.RESCHEDULED)));

            assertThatThrownBy(() -> service.rescheduleSession(SCHEDULE_ID,
                    new ScheduleRescheduleDTO(START, END)))
                    .isInstanceOf(ScheduleAlreadyRescheduledException.class);
        }

        @Test
        @DisplayName("deve lançar ScheduleConflictTimeException quando nova data conflita")
        void deveLancarQuandoNovaDataConflita() {
            var original = openedSchedule();
            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(original));
            when(scheduleRepo.findConflictingSchedules(any(), any(), eq(SCHEDULE_ID)))
                    .thenReturn(List.of(openedSchedule()));

            assertThatThrownBy(() -> service.rescheduleSession(SCHEDULE_ID,
                    new ScheduleRescheduleDTO(START.plusDays(1), END.plusDays(1))))
                    .isInstanceOf(ScheduleConflictTimeException.class);
        }

        @Test
        @DisplayName("nova sessão deve herdar paciente e plano da sessão original")
        void devePropagaPacientEPlanoParaNovaSessao() {
            var plan = new Plan();
            var original = openedSchedule();
            original.setType(AttendanceTypeEnum.REMOTE);
            original.setPlan(plan);
            var newStart = START.plusDays(1);
            var dto = new ScheduleRescheduleDTO(newStart, newStart.plusHours(1));

            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(original));
            when(scheduleRepo.findConflictingSchedules(any(), any(), eq(SCHEDULE_ID))).thenReturn(List.of());

            service.rescheduleSession(SCHEDULE_ID, dto);

            // Captura todas as chamadas ao save e verifica que a nova sessão herdou o plano
            ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
            verify(scheduleRepo, times(2)).save(captor.capture());
            List<Schedule> saved = captor.getAllValues();
            boolean newSessionHasPlan = saved.stream()
                    .anyMatch(s -> s != original && plan.equals(s.getPlan()));
            assertThat(newSessionHasPlan).isTrue();
        }

        @Test
        @DisplayName("nova sessão deve herdar o organizationId da original (senão some do calendário)")
        void deveHerdarOrganizationId() {
            var original = openedSchedule();
            original.setOrganizationId("org-1");
            var newStart = START.plusDays(1);
            var dto = new ScheduleRescheduleDTO(newStart, newStart.plusHours(1));

            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(original));
            when(scheduleRepo.findConflictingSchedules(any(), any(), eq(SCHEDULE_ID))).thenReturn(List.of());

            service.rescheduleSession(SCHEDULE_ID, dto);

            ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
            verify(scheduleRepo, times(2)).save(captor.capture());
            Schedule newSession = captor.getAllValues().stream()
                    .filter(s -> s != original).findFirst().orElseThrow();
            assertThat(newSession.getOrganizationId()).isEqualTo("org-1");
        }

        @Test
        @DisplayName("deve resolver dateEnd como start + 1h quando dateEnd é null no reagendamento")
        void deveResolverDateEndQuandoNuloNoReagendamento() {
            var original = openedSchedule();
            original.setType(AttendanceTypeEnum.PRESENTIAL);
            var newStart = START.plusDays(1);
            var dto = new ScheduleRescheduleDTO(newStart, null);

            when(scheduleRepo.findById(SCHEDULE_ID)).thenReturn(Optional.of(original));
            when(scheduleRepo.findConflictingSchedules(eq(newStart), eq(newStart.plusHours(1)), eq(SCHEDULE_ID)))
                    .thenReturn(List.of());

            assertThatCode(() -> service.rescheduleSession(SCHEDULE_ID, dto))
                    .doesNotThrowAnyException();

            verify(scheduleRepo).findConflictingSchedules(newStart, newStart.plusHours(1), SCHEDULE_ID);
        }
    }

    // endregion
}
