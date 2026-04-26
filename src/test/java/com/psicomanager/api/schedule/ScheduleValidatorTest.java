package com.psicomanager.api.schedule;

import com.psicomanager.api.core.exception.BusinessRuleException;
import com.psicomanager.api.schedule.dto.ScheduleRegisterDTO;
import com.psicomanager.api.schedule.dto.ScheduleRescheduleDTO;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import com.psicomanager.api.schedule.validation.ScheduleValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("ScheduleValidator")
class ScheduleValidatorTest {

    // region Helpers

    private static final LocalDateTime START = LocalDateTime.of(2025, 4, 25, 14, 0);
    private static final LocalDateTime END_VALID = START.plusHours(1);
    private static final LocalDateTime END_INVALID = START.minusMinutes(30);

    private static ScheduleRegisterDTO dto(LocalDateTime start, LocalDateTime end,
                                           Integer sessionsCount, FrequencyEnum frequency) {
        return new ScheduleRegisterDTO("patient-1", start, end, null, null, null, frequency, sessionsCount, null);
    }

    // endregion

    // region validateRegister

    @Nested
    @DisplayName("validateRegister")
    class ValidateRegister {

        @Test
        @DisplayName("deve passar quando start e end são válidos e sessão única")
        void devePassarQuandoSessaoUnicaValida() {
            assertThatCode(() -> ScheduleValidator.validateRegister(dto(START, END_VALID, null, null)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve passar quando dateEnd é null (sem fim definido)")
        void devePassarQuandoDateEndNulo() {
            assertThatCode(() -> ScheduleValidator.validateRegister(dto(START, null, null, null)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar BusinessRuleException quando end não é posterior ao start")
        void deveLancarQuandoEndNaoEPosterior() {
            assertThatThrownBy(() -> ScheduleValidator.validateRegister(dto(START, END_INVALID, null, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("posterior");
        }

        @Test
        @DisplayName("deve lançar BusinessRuleException quando end é igual ao start")
        void deveLancarQuandoEndIgualAoStart() {
            assertThatThrownBy(() -> ScheduleValidator.validateRegister(dto(START, START, null, null)))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("deve passar quando sessionsCount > 1 e frequency está informada")
        void devePassarQuandoLoteComFrequencia() {
            assertThatCode(() -> ScheduleValidator.validateRegister(dto(START, null, 5, FrequencyEnum.WEEKLY)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar BusinessRuleException quando sessionsCount > 1 sem frequency")
        void deveLancarQuandoLoteSemFrequencia() {
            assertThatThrownBy(() -> ScheduleValidator.validateRegister(dto(START, null, 5, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("frequência");
        }

        @Test
        @DisplayName("não deve exigir frequency quando sessionsCount é 1")
        void naoDeveExigirFrequenciaParaSessaoUnica() {
            assertThatCode(() -> ScheduleValidator.validateRegister(dto(START, null, 1, null)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("não deve exigir frequency quando sessionsCount é null")
        void naoDeveExigirFrequenciaQuandoSessionsCountNulo() {
            assertThatCode(() -> ScheduleValidator.validateRegister(dto(START, null, null, null)))
                    .doesNotThrowAnyException();
        }
    }

    // endregion

    // region validateReschedule

    @Nested
    @DisplayName("validateReschedule")
    class ValidateReschedule {

        @Test
        @DisplayName("deve passar quando dateEnd é null")
        void devePassarQuandoDateEndNulo() {
            var dto = new ScheduleRescheduleDTO(START, null);
            assertThatCode(() -> ScheduleValidator.validateReschedule(dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve passar quando dateEnd é posterior ao dateStart")
        void devePassarQuandoDateEndValido() {
            var dto = new ScheduleRescheduleDTO(START, END_VALID);
            assertThatCode(() -> ScheduleValidator.validateReschedule(dto))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar BusinessRuleException quando dateEnd não é posterior ao dateStart")
        void deveLancarQuandoDateEndInvalido() {
            var dto = new ScheduleRescheduleDTO(START, END_INVALID);
            assertThatThrownBy(() -> ScheduleValidator.validateReschedule(dto))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("posterior");
        }

        @Test
        @DisplayName("deve lançar BusinessRuleException quando dateEnd é igual ao dateStart")
        void deveLancarQuandoDateEndIgualAoStart() {
            var dto = new ScheduleRescheduleDTO(START, START);
            assertThatThrownBy(() -> ScheduleValidator.validateReschedule(dto))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // endregion
}
