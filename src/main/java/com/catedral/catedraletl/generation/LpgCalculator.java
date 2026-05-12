package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.DeduccionDTO;
import com.catedral.catedraletl.parsing.LiquidacionDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class LpgCalculator {

    private final TxtFormatter txtFormatter;

    public LpgCalculator(TxtFormatter txtFormatter){
        this.txtFormatter = txtFormatter;
    }

    public BigDecimal totalDedducciones(LiquidacionDTO liquidacionDTO){
        return liquidacionDTO.getDeducciones().stream()
                .map(DeduccionDTO::getBaseCalculo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal neto(BigDecimal totalDeducciones, LiquidacionDTO liquidacionDTO){
        return liquidacionDTO.getSubtotalBruto().subtract(totalDeducciones);
    }

    public BigDecimal iva(BigDecimal neto){
        return neto.multiply(new BigDecimal("0.105")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal total(BigDecimal neto, BigDecimal iva){
        return neto.add(iva);
    }

    public String netoStr(BigDecimal total){
        return txtFormatter.formatImporte(total, 15);
    }

    public String totalStr(BigDecimal total){
        return txtFormatter.formatImporte(total, 15);
    }

    public String ivaStr(BigDecimal iva){
        return txtFormatter.formatImporte(iva, 15);
    }

    public String pVenta(LiquidacionDTO liquidacionDTO){
        return liquidacionDTO.getCoe().substring(0,4);
    }

    public String numComprobante(LiquidacionDTO liquidacionDTO){
        return liquidacionDTO.getCoe().substring(4);
    }

    public String fechaFormat(LiquidacionDTO liquidacionDTO){
        return txtFormatter.formatFecha(liquidacionDTO.getFecha());
    }

    public String razonFormat(LiquidacionDTO liquidacionDTO){
        return txtFormatter.formatText(liquidacionDTO.getRazonSocialComprador(), 30);
    }

}
