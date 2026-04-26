package com.psicomanager.api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DateUtils")
class DateUtilsTest {

    @Test
    @DisplayName("deve retornar data por extenso no padrão 'D de Mês de YYYY'")
    void deveRetornarDataPorExtenso() {
        var result = DateUtils.getDateToContract();

        // Valida formato sem fixar o valor exato (data é dinâmica)
        assertThat(result).matches("\\d{1,2} de [A-Za-zçãõ]+ de \\d{4}");
    }

    @Test
    @DisplayName("deve conter o ano atual")
    void deveConterAnoAtual() {
        var result = DateUtils.getDateToContract();
        var currentYear = java.time.LocalDate.now().getYear();

        assertThat(result).contains(String.valueOf(currentYear));
    }

    @Test
    @DisplayName("deve conter o dia atual")
    void deveConterDiaAtual() {
        var result = DateUtils.getDateToContract();
        var currentDay = java.time.LocalDate.now().getDayOfMonth();

        assertThat(result).startsWith(String.valueOf(currentDay));
    }

    @Test
    @DisplayName("deve conter nome do mês em português")
    void deveConterNomeDoMesEmPortugues() {
        var result = DateUtils.getDateToContract();
        var meses = java.util.List.of(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        );

        assertThat(meses).anyMatch(result::contains);
    }

    @Test
    @DisplayName("deve retornar string não nula e não vazia")
    void deveRetornarStringValida() {
        assertThat(DateUtils.getDateToContract()).isNotNull().isNotBlank();
    }
}
