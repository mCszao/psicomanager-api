package com.psicomanager.api.plan.dto;

import com.psicomanager.api.schedule.enums.AttendanceTypeEnum;
import com.psicomanager.api.schedule.enums.FrequencyEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload de registro de um novo plano de atendimento para um paciente.
 * <p>
 * Quando {@code planTemplateId} é informado, os campos {@code pricePerSession},
 * {@code sessionsCount} e {@code frequency} são herdados do template e só
 * precisam ser enviados caso o psicólogo queira sobrescrevê-los para este paciente.
 * </p>
 *
 * <h3>Regras de negócio validadas no service:</h3>
 * <ul>
 *   <li>Plano <b>finito</b> ({@code isContinuous = false}): {@code frequency} e
 *       {@code sessionsCount} são obrigatórios.</li>
 *   <li>Plano <b>contínuo</b> ({@code isContinuous = true}): {@code frequency}
 *       é obrigatória.</li>
 *   <li>Quando {@code generateSessions = true}: {@code sessionStartTime} e
 *       {@code attendanceType} são obrigatórios e {@code frequency} deve estar definida.</li>
 * </ul>
 */
public record PlanRegisterDTO(

        @NotBlank
        String patientId,

        /** ID do template a ser usado como base. Opcional. */
        String planTemplateId,

        /** Título do plano. Opcional — sobrescreve o título do template se informado. */
        String title,

        /**
         * Valor por sessão. Opcional — pré-preenchido a partir do template quando disponível.
         * Pode ser sobrescrito para ajustes individuais por paciente.
         */
        BigDecimal pricePerSession,

        /**
         * Quantidade de sessões. Obrigatório para planos finitos.
         * Opcional para contínuos — usado apenas como referência de geração.
         */
        @Min(1)
        Integer sessionsCount,

        /**
         * Frequência das sessões. Obrigatória para qualquer tipo de plano.
         * Determina o espaçamento entre sessões geradas e o funcionamento
         * do botão "Lançar mais sessões".
         */
        FrequencyEnum frequency,

        @NotNull
        LocalDate adherenceDate,

        /**
         * Data estimada de encerramento. Opcional.
         * Quando não informada e houver {@code frequency} + {@code sessionsCount},
         * é calculada automaticamente.
         */
        LocalDate estimatedEndDate,

        /**
         * Quando {@code true}, o plano é contínuo e não encerra automaticamente.
         * Padrão: {@code true}.
         */
        boolean isContinuous,

        /**
         * Quando {@code true}, gera as sessões automaticamente com base em
         * {@code frequency} e {@code sessionsCount} (finitos) ou ~3 meses (contínuos)
         * a partir de {@code adherenceDate}. Requer {@code sessionStartTime} e {@code attendanceType}.
         */
        boolean generateSessions,

        /**
         * Horário de início de cada sessão gerada (formato {@code HH:mm}).
         * Obrigatório quando {@code generateSessions} é {@code true}.
         */
        String sessionStartTime,

        /**
         * Tipo de atendimento das sessões geradas (presencial ou remoto).
         * Obrigatório quando {@code generateSessions} é {@code true}.
         * Também é armazenado no plano para reverberar em futuras sessões lançadas.
         */
        AttendanceTypeEnum attendanceType

) {}
