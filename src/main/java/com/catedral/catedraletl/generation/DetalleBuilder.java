package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.LiquidacionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DetalleBuilder {

    private final TxtFormatter txtFormatter;
    private final LpgCalculator lpgCalculator;
    public DetalleBuilder(TxtFormatter txtFormatter, LpgCalculator lpgCalculator) {
        this.txtFormatter = txtFormatter;
        this.lpgCalculator = lpgCalculator;
    }

    public String buildLine(LiquidacionDTO liquidacionDTO){

        BigDecimal totalDeducciones = lpgCalculator.totalDedducciones(liquidacionDTO);
        BigDecimal neto = lpgCalculator.neto(totalDeducciones, liquidacionDTO);
        BigDecimal iva = lpgCalculator.iva(neto);
        BigDecimal total = lpgCalculator.total(neto, iva);

        String tipoComp = "01 ";
        String fecha = lpgCalculator.fechaFormat(liquidacionDTO);
        String pVenta = lpgCalculator.pVenta(liquidacionDTO);
        String numComprobante = lpgCalculator.numComprobante(liquidacionDTO);
        String constFija = "00000010000007";
        String netoStr = lpgCalculator.netoStr(neto);
        String noGravado = StringUtils.repeat("0", 15);
        String ivaCeros = StringUtils.repeat("0", 15);
        String filler = "00";
        String totalStr = lpgCalculator.totalStr(total);
        String alicuota = "0105";
        String filler2 = "0G ";
        String concepto = txtFormatter.formatText("LIQUIDACION PRIMARIA DE GRANOS", 76);

        return new StringBuilder()
                .append(tipoComp)
                .append(fecha)
                .append(pVenta)
                .append(numComprobante)
                .append(numComprobante)
                .append(constFija)
                .append(netoStr)
                .append(noGravado)
                .append(ivaCeros)
                .append(filler)
                .append(totalStr)
                .append(alicuota)
                .append(filler2)
                .append(concepto)
                .toString();
    }

}
