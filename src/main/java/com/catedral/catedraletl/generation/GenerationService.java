package com.catedral.catedraletl.generation;

import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GenerationService {

    private final CabeceraBuilder cabeceraBuilder;
    private final DetalleBuilder detalleBuilder;

    public GenerationService(CabeceraBuilder cabeceraBuilder, DetalleBuilder detalleBuilder){
        this.cabeceraBuilder = cabeceraBuilder;
        this.detalleBuilder = detalleBuilder;
    }

    public TxtResultDTO generate(LpgDocumentDTO document) {
        String cabecera = document.getLiquidaciones().stream()
                .map(cabeceraBuilder::buildType1)
                .collect(Collectors.joining("\n")) + "\n" + cabeceraBuilder.buildType2(document);
        String detalle = document.getLiquidaciones().stream()
                .map(detalleBuilder::buildLine)
                .collect(Collectors.joining("\n"));

        return TxtResultDTO.builder()
                .cabecera(cabecera)
                .detalle(detalle)
                .build();
    }
}
