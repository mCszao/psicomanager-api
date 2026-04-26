package com.psicomanager.api.plan;

import com.psicomanager.api.core.exception.BusinessRuleException;
import com.psicomanager.api.plan.dto.PlanRegisterDTO;
import com.psicomanager.api.plan.model.Plan;
import com.psicomanager.api.plan.validation.PlanValidator;
import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("PlanValidator")
class PlanValidatorTest {

    // region Helpers

    private static final LocalDate TODAY = LocalDate.of(2025, 4, 25);

    private static PlanRegisterDTO dto(boolean isContinuous, boolean generateSessions,
                                       Integer sessionsCount, FrequencyEnum frequency,
                                       String sessionStartTime, AttendanceTypeEnum attendanceType) {
        return new PlanRegisterDTO(
                "patient-1", null, "Plano Teste", null,
                sessionsCount, frequency, TODAY, null,
                isContinuous, generateSessions,
                sessionStartTime, attendanceType
        );
    }

    // endregion

    // region validateRegister

    @Nested
    @DisplayName("validateRegister")
    class ValidateRegister {

        @Test
        @DisplayName("deve lançar quando frequency é null em qualquer plano")
        void deveLancarQuandoFrequencyNula() {
            var dto = dto(true, false, null, null, null, null);
            assertThatThrownBy(() -> PlanValidator.validateRegister(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("frequência");
        }

        @Test
        @DisplayName("deve lançar quando plano finito sem sessionsCount")
        void deveLancarQuandoPlanoFinitoSemSessionsCount() {
            var dto = dto(false, false, null, FrequencyEnum.WEEKLY, null, null);
            assertThatThrownBy(() -> PlanValidator.validateRegister(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("sessões");
        }

        @Test
        @DisplayName("deve lançar quando plano finito com sessionsCount menor que 1")
        void deveLancarQuandoPlanoFinitoComSessionsCountZero() {
            var dto = dto(false, false, 0, FrequencyEnum.WEEKLY, null, null);
            assertThatThrownBy(() -> PlanValidator.validateRegister(dto))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("deve passar para plano finito válido sem geração de sessões")
        void devePassarParaPlanoFinitoValido() {
            var dto = dto(false, false, 10, FrequencyEnum.WEEKLY, null, null);
            assertThatCode(() -> PlanValidator.validateRegister(dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve passar para plano contínuo com apenas frequency")
        void devePassarParaPlanoContinuoComFrequency() {
            var dto = dto(true, false, null, FrequencyEnum.MONTHLY, null, null);
            assertThatCode(() -> PlanValidator.validateRegister(dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar quando generateSessions=true sem sessionsCount")
        void deveLancarQuandoGerarSessoesEmSessaosSemCount() {
            var dto = dto(true, true, null, FrequencyEnum.WEEKLY, "09:00", AttendanceTypeEnum.PRESENTIAL);
            assertThatThrownBy(() -> PlanValidator.validateRegister(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("número de sessões");
        }

        @Test
        @DisplayName("deve lançar quando generateSessions=true sem sessionStartTime")
        void deveLancarQuandoGerarSessoeSemHorario() {
            var dto = dto(false, true, 10, FrequencyEnum.WEEKLY, null, AttendanceTypeEnum.PRESENTIAL);
            assertThatThrownBy(() -> PlanValidator.validateRegister(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("horário");
        }

        @Test
        @DisplayName("deve lançar quando generateSessions=true sem attendanceType")
        void deveLancarQuandoGerarSessoesSemTipo() {
            var dto = dto(false, true, 10, FrequencyEnum.WEEKLY, "09:00", null);
            assertThatThrownBy(() -> PlanValidator.validateRegister(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("tipo de atendimento");
        }

        @Test
        @DisplayName("deve passar quando generateSessions=true com todos os campos obrigatórios")
        void devePassarQuandoGerarSessoesCompleto() {
            var dto = dto(false, true, 10, FrequencyEnum.WEEKLY, "09:00", AttendanceTypeEnum.PRESENTIAL);
            assertThatCode(() -> PlanValidator.validateRegister(dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve passar quando generateSessions=false independente de sessionStartTime e attendanceType")
        void devePassarQuandoNaoGerarSessoes() {
            var dto = dto(false, false, 10, FrequencyEnum.DAILY, null, null);
            assertThatCode(() -> PlanValidator.validateRegister(dto))
                    .doesNotThrowAnyException();
        }
    }

    // endregion

    // region validatePlanIsActive

    @Nested
    @DisplayName("validatePlanIsActive")
    class ValidatePlanIsActive {

        @Test
        @DisplayName("deve lançar BusinessRuleException quando plano está inativo")
        void deveLancarQuandoPlanoInativo() {
            var plan = new Plan();
            plan.setIsActive(false);

            assertThatThrownBy(() -> PlanValidator.validatePlanIsActive(plan))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("encerrado");
        }

        @Test
        @DisplayName("deve passar quando plano está ativo")
        void devePassarQuandoPlanoAtivo() {
            var plan = new Plan();
            plan.setIsActive(true);

            assertThatCode(() -> PlanValidator.validatePlanIsActive(plan))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar quando isActive é null (tratado como inativo)")
        void deveLancarQuandoIsActiveNulo() {
            var plan = new Plan();
            plan.setIsActive(null);

            assertThatThrownBy(() -> PlanValidator.validatePlanIsActive(plan))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // endregion
}
