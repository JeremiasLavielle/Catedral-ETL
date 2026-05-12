package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.LiquidacionDTO;
import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import com.catedral.catedraletl.parsing.LpgParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DetalleBuilderTest {
    private DetalleBuilder detalleBuilder;
    private LpgDocumentDTO document;

    @BeforeEach
    void setup() throws IOException {
        TxtFormatter formatter = new TxtFormatter();
        LpgCalculator calculator = new LpgCalculator(formatter);
        detalleBuilder = new DetalleBuilder(formatter, calculator);

        byte[] pdfBytes = getClass()
                .getResourceAsStream("/Liquidaciones HORIS 022026.pdf")
                .readAllBytes();

        LpgParserService parserService = new LpgParserService();
        document = parserService.parse(pdfBytes);
    }

    @Test
    void shouldBuildDetalleLine() {
        for (LiquidacionDTO liquidacion : document.getLiquidaciones()) {
            String linea = detalleBuilder.buildLine(liquidacion);
            System.out.println(linea);
        }
    }
}
