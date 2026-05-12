package com.catedral.catedraletl.parsing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class LpgParserServiceTest {

    private LpgParserService service;
    private byte[] pdfBytes;

    @BeforeEach
    void setUp() throws IOException {
        service = new LpgParserService();
        pdfBytes = getClass()
                .getResourceAsStream("/Liquidaciones HORIS 022026.pdf")
                .readAllBytes();
    }


    @Test
    void shouldSplitInto7Blocks() throws IOException {
        String rawText = service.extractText(pdfBytes);
        List<String> blocks = service.splitIntoBlocks(rawText);
        blocks.forEach(block -> System.out.println("=== BLOCK ===\n" + block));
    }

    @Test
    void shouldReturnBlockExtraction() throws IOException {
        String rawText = service.extractText(pdfBytes);
        List<String> blocks = service.splitIntoBlocks(rawText);
        for (String block : blocks) {
            LiquidacionDTO dto = service.parseLiquidacionDTO(block);
            System.out.println(dto);
        }
    }

    @Test
    void shouldReturnLpgDocumentDTO() throws IOException {
        System.out.println(service.parse(pdfBytes));
    }

}
