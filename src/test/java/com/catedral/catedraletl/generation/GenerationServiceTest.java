package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import com.catedral.catedraletl.parsing.LpgParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class GenerationServiceTest {

    private GenerationService generationService;
    private LpgDocumentDTO document;

    @BeforeEach
    void setUp() throws IOException {
        TxtFormatter formatter = new TxtFormatter();
        LpgCalculator calculator = new LpgCalculator(formatter);
        CabeceraBuilder cabeceraBuilder = new CabeceraBuilder(formatter, calculator);
        DetalleBuilder detalleBuilder = new DetalleBuilder(formatter, calculator);
        generationService = new GenerationService(cabeceraBuilder, detalleBuilder);

        byte[] pdfBytes = getClass()
                .getResourceAsStream("/Liquidaciones HORIS 022026.pdf")
                .readAllBytes();
        LpgParserService parserService = new LpgParserService();
        document = parserService.parse(pdfBytes);
    }

    @Test
    void shouldGenerate() throws IOException {
        TxtResultDTO result = generationService.generate(document);
        System.out.println("=== CABECERA ===");
        System.out.println(result.getCabecera());
        System.out.println("=== DETALLE ===");
        System.out.println(result.getDetalle());
    }
}
