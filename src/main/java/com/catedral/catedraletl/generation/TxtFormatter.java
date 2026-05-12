package com.catedral.catedraletl.generation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class TxtFormatter {
    public String formatFecha(String fecha) {
        LocalDate date = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public String formatImporte(BigDecimal importe, int longitud) {
        long centavos = importe.movePointRight(2).setScale(0, RoundingMode.HALF_DOWN).longValueExact();
        return StringUtils.leftPad(String.valueOf(centavos), longitud, '0');
    }

    public String formatEntero(long valor, int longitud) {
        return StringUtils.leftPad(String.valueOf(valor), longitud, '0');
    }

    public String formatText(String text,  int longitud) {
        return  StringUtils.rightPad(text, longitud, ' ');
    }
}
