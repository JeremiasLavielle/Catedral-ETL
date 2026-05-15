package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.LiquidacionDTO;
import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CabeceraBuilder {

    private final TxtFormatter txtFormatter;
    private final LpgCalculator lpgCalculator;

    public CabeceraBuilder(TxtFormatter txtFormatter, LpgCalculator lpgCalculator) {
        this.txtFormatter = txtFormatter;
        this.lpgCalculator = lpgCalculator;
    }

    public String buildType1(LiquidacionDTO liquidacionDTO) {

        BigDecimal totalDeducciones = lpgCalculator.totalDedducciones(liquidacionDTO);
        BigDecimal neto = lpgCalculator.neto(totalDeducciones, liquidacionDTO);
        BigDecimal iva = lpgCalculator.iva(neto);
        BigDecimal total = lpgCalculator.total(neto, iva);

        String tipo = "1";
        String fecha = lpgCalculator.fechaFormat(liquidacionDTO);
        String tipoComprobante = "01 ";
        String pVenta = lpgCalculator.pVenta(liquidacionDTO);
        String numComprobante = lpgCalculator.numComprobante(liquidacionDTO);
        String codDoc = "001";
        String filler = "80";
        String cuitComprador = liquidacionDTO.getCuitComprador();
        String razonSocial = lpgCalculator.razonFormat(liquidacionDTO);
        String totalStr = lpgCalculator.totalStr(total);
        String noGravado = "000000000000000";
        String netoStr = lpgCalculator.netoStr(neto);
        String ivaStr = lpgCalculator.ivaStr(iva);
        String filler2 = StringUtils.repeat("0", 104) + "1";
        String moneda = "PES";
        String tipCambio = "0001000000";
        String filler3 = "1";
        String espacio = " ";
        String cae = StringUtils.repeat("0", 14);
        String fechaVencimiento = lpgCalculator.fechaFormat(liquidacionDTO) + StringUtils.repeat(" ", 8);

        return new StringBuilder()
                .append(tipo)
                .append(fecha)
                .append(tipoComprobante)
                .append(pVenta)
                .append(numComprobante)
                .append(numComprobante)
                .append(codDoc)
                .append(filler)
                .append(cuitComprador)
                .append(razonSocial)
                .append(totalStr)
                .append(noGravado)
                .append(netoStr)
                .append(ivaStr)
                .append(filler2)
                .append(moneda)
                .append(tipCambio)
                .append(filler3)
                .append(espacio)
                .append(cae)
                .append(fechaVencimiento)
                .toString();
    }

    public String buildType2(LpgDocumentDTO lpgDocumentDTO) {
        List<LiquidacionDTO> liquidaciones = lpgDocumentDTO.getLiquidaciones();

        BigDecimal sumaTotal = liquidaciones.stream()
                .map(liq -> lpgCalculator.total(
                        lpgCalculator.neto(lpgCalculator.totalDedducciones(liq), liq),
                        lpgCalculator.iva(lpgCalculator.neto(lpgCalculator.totalDedducciones(liq), liq))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumaIva = liquidaciones.stream()
                .map(liq -> lpgCalculator.iva(lpgCalculator.neto(lpgCalculator.totalDedducciones(liq), liq)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumaNeto = liquidaciones.stream()
                .map(liq -> lpgCalculator.neto(lpgCalculator.totalDedducciones(liq), liq))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String tipo = "2";
        String periodo = lpgDocumentDTO.getPeriodo();
        String espacios = StringUtils.repeat(" ", 13);
        String cantidadComprobantes = txtFormatter.formatEntero(liquidaciones.size(), 8);
        String espacios2 = StringUtils.repeat(" ", 17);
        String cuitVendedor = lpgDocumentDTO.getCuitVendedor();
        String espacios3 = StringUtils.repeat(" ", 22);
        String totales = txtFormatter.formatImporte(sumaTotal, 15);
        String ceros = StringUtils.repeat("0", 15);
        String netos = txtFormatter.formatImporte(sumaNeto, 15);
        String ivas = txtFormatter.formatImporte(sumaIva, 15);
        String ceros2 = StringUtils.repeat("0", 120);

        return new StringBuilder()
                .append(tipo)
                .append(periodo)
                .append(espacios)
                .append(cantidadComprobantes)
                .append(espacios2)
                .append(cuitVendedor)
                .append(espacios3)
                .append(totales)
                .append(ceros)
                .append(netos)
                .append(ivas)
                .append(ceros2)
                .toString();
    }
}
