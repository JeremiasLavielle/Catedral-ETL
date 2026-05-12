package com.catedral.catedraletl.export;

import com.catedral.catedraletl.generation.*;
import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import com.catedral.catedraletl.parsing.LpgParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ExportServiceTest {

    private ExportService exportService;
    private GenerationService generationService;
    private LpgDocumentDTO document;

    @BeforeEach
    void setUp() throws IOException {
        TxtFormatter formatter = new TxtFormatter();
        LpgCalculator calculator = new LpgCalculator(formatter);
        CabeceraBuilder cabeceraBuilder = new CabeceraBuilder(formatter, calculator);
        DetalleBuilder detalleBuilder = new DetalleBuilder(formatter, calculator);
        generationService = new GenerationService(cabeceraBuilder, detalleBuilder);
        exportService = new ExportService();

        byte[] pdfBytes = getClass()
                .getResourceAsStream("/Liquidaciones HORIS 022026.pdf")
                .readAllBytes();
        LpgParserService parserService = new LpgParserService();
        document = parserService.parse(pdfBytes);
    }

    @Test
    void shouldExportZip() throws IOException {
        TxtResultDTO result = generationService.generate(document);
        ZipResultDTO zip = exportService.export(result);

        Files.write(Path.of("/tmp/lpg_test.zip"), zip.getZipBytes());
        System.out.println("ZIP generado en /tmp/lpg_test.zip");
    }
}