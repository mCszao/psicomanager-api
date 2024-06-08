package com.psicomanager.api.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {

    public static String getDateToContract(){
        var dateTime = LocalDate.now();
        String phrase = "{day} de {month} de {year}";
        Map<Month, String> months = DateUtils.getMapMonth();
        phrase = phrase.replace("{day}", String.valueOf(dateTime.getDayOfMonth()));
        phrase = phrase.replace("{month}", months.get(dateTime.getMonth()));
        phrase = phrase.replace("{year}", String.valueOf(dateTime.getYear()));
        return phrase;
    }

    private static Map<Month, String> getMapMonth(){
        Map<Month, String> monthMapping = new HashMap<>();
        monthMapping.put(Month.JANUARY, "Janeiro");
        monthMapping.put(Month.FEBRUARY, "Fevereiro");
        monthMapping.put(Month.MARCH, "Março");
        monthMapping.put(Month.APRIL, "Abril");
        monthMapping.put(Month.MAY, "Maio");
        monthMapping.put(Month.JUNE, "Junho");
        monthMapping.put(Month.JULY, "Julho");
        monthMapping.put(Month.AUGUST, "Agosto");
        monthMapping.put(Month.SEPTEMBER, "Setembro");
        monthMapping.put(Month.OCTOBER, "Outubro");
        monthMapping.put(Month.NOVEMBER, "Novembro");
        monthMapping.put(Month.DECEMBER, "Dezembro");
        return monthMapping;
    }
}
