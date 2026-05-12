package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.LiquidacionDTO;
import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import com.catedral.catedraletl.parsing.LpgParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CabeceraBuilderTest {

    private CabeceraBuilder cabeceraBuilder;
    private LpgDocumentDTO document;

    @BeforeEach
    void setup() throws IOException {
        TxtFormatter formatter = new TxtFormatter();
        LpgCalculator calculator = new LpgCalculator(formatter);
        cabeceraBuilder = new CabeceraBuilder(formatter, calculator);

        byte[] pdfBytes = getClass()
                .getResourceAsStream("/Liquidaciones HORIS 022026.pdf")
                .readAllBytes();

        LpgParserService parserService = new LpgParserService();
        document = parserService.parse(pdfBytes);
    }

    @Test
    void shouldBuildType1(){
        for(LiquidacionDTO liquidacion : document.getLiquidaciones()){
            String linea = cabeceraBuilder.buildType1(liquidacion);
            System.out.println(linea);
        }
    }

    @Test
    void shouldBuildType2(){
        String linea = cabeceraBuilder.buildType2(document);
        System.out.println(linea);
    }

}
