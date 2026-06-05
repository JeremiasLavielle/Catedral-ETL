package com.catedral.catedraletl.parsing;

import com.catedral.catedraletl.exception.LpgParseException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LpgParserService {

    // ----- Parser Orchestrator -----

    public LpgDocumentDTO parse(byte[] pdfBytes) throws IOException {
        String rawText = extractText(pdfBytes);
        List<String> blocks = splitIntoBlocks(rawText);

        List<LiquidacionDTO> liquidaciones = blocks.stream()
                .map(this::parseLiquidacionDTO)
                .toList();

        String fechaStr = liquidaciones.getFirst().getFecha();
        LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String periodo = fecha.format(DateTimeFormatter.ofPattern("yyyyMM"));

        return LpgDocumentDTO.builder()
                .liquidaciones(liquidaciones)
                .cuitVendedor(extractCuitVendedor(blocks.getFirst()))
                .periodo(periodo)
                .build();
    }

    public String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    // ----- Blocks Parser -----

    public List<String> splitIntoBlocks(String rawText) {
        List<String> blocks = Arrays.stream(rawText.split("(?=\\d{2}/\\d{2}/\\d{4},)"))
                .filter(block -> block.contains("1 / 2") || block.contains("1 / 1"))
                .filter(block -> !block.contains("Ajuste unificado"))
                .toList();
        if (blocks.isEmpty()) {
            throw new LpgParseException(
                    "El PDF no contiene liquidaciones válidas. Verificá que el archivo sea una Liquidación Primaria de Granos y no esté vacío o corrupto.");
        }
        return blocks;
    }

    // ----- Dto Generation -----
    public LiquidacionDTO parseLiquidacionDTO(String block) {
        return LiquidacionDTO.builder()
                .coe(extractCoe(block))
                .fecha(extractDate(block))
                .cuitComprador(extractCuitComprador(block))
                .subtotalBruto(extractSubtotal(block))
                .deducciones(extractDeducciones(block))
                .razonSocialComprador(extractRazonSocial(block))
                .build();
    }

    // ----- Regex Extraction by block -----

    private String extractCoe(String block) {
        Pattern datePattern = Pattern.compile("C\\.O\\.E\\.: (\\d{12})");
        Matcher dateMatcher = datePattern.matcher(block);
        if (dateMatcher.find()) {
            return dateMatcher.group(1);
        }
        throw new LpgParseException(
                "No se encontró el C.O.E. en la liquidación. Se esperaba un número de 12 dígitos con formato 'C.O.E.: XXXXXXXXXXXX'.");
    }

    private String extractDate(String block) {
        Pattern datePattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
        Matcher dateMatcher = datePattern.matcher(block);
        if (dateMatcher.find()) {
            return dateMatcher.group(1);
        }
        throw new LpgParseException(
                "No se encontró la fecha en la liquidación. Se esperaba formato 'dd/MM/yyyy'.");
    }

    private String extractCuitComprador(String block) {
        Pattern cuitCompradorPattern = Pattern.compile("C\\.U\\.I\\.T\\.: (\\d{11})");
        Matcher cuitCompradorMatcher = cuitCompradorPattern.matcher(block);
        if (cuitCompradorMatcher.find()) {
            return cuitCompradorMatcher.group(1);
        }
        throw new LpgParseException(
                "No se encontró el CUIT del comprador en la liquidación. Se esperaba un número de 11 dígitos con formato 'C.U.I.T.: XXXXXXXXXXX'.");
    }

    private String extractCuitVendedor(String block) {
        Pattern cuitVendedorPattern = Pattern.compile("C\\.U\\.I\\.T\\.: (\\d{11})");
        Matcher cuitVendedorMatcher = cuitVendedorPattern.matcher(block);
        if (cuitVendedorMatcher.find()) {
            if (cuitVendedorMatcher.find()) {
                return cuitVendedorMatcher.group(1);
            }
        }
        throw new LpgParseException(
                "No se encontró el CUIT del vendedor en la liquidación. El documento debe contener dos CUITs: el del comprador y el del vendedor.");
    }

    private BigDecimal extractSubtotal(String block) {
        Pattern subtotalPattern = Pattern.compile("Kg \\$[\\d.,]+ \\$([\\d.,]+)");
        Matcher subtotalMatcher = subtotalPattern.matcher(block);
        if (subtotalMatcher.find()) {
            String subtotal = subtotalMatcher.group(1).replace(",", "");
            try {
                return new BigDecimal(subtotal);
            } catch (NumberFormatException e) {
                throw new LpgParseException(
                        "El subtotal bruto encontrado en la liquidación tiene un formato inválido: '" + subtotal + "'. Se esperaba un número decimal.", e);
            }
        }
        throw new LpgParseException(
                "No se encontró el subtotal bruto en la liquidación. Se esperaba una línea con formato 'Kg $[precio] $[subtotal]'.");
    }

    private List<DeduccionDTO> extractDeducciones(String block) {
        List<DeduccionDTO> deducciones = new ArrayList<>();
        Pattern deduccionesPattern = Pattern.compile("\\$\\s?([\\d.,]+)\\s+10\\.5%");
        Matcher deduccionesMatcher = deduccionesPattern.matcher(block);
        while (deduccionesMatcher.find()) {
            String deduccion = deduccionesMatcher.group(1).replace(",", "");
            DeduccionDTO build = DeduccionDTO.builder().
                    baseCalculo(new BigDecimal(deduccion))
                    .build();
            deducciones.add(build);
        }
        return deducciones;
    }

    private String extractRazonSocial(String block) {
        Pattern pattern = Pattern.compile(
                "Razón Social:\\s*(.*?)\\s*(?=Razón Social:)",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(block);

        if (!matcher.find()) {
            throw new LpgParseException(
                    "No se encontró la Razón Social del comprador en la liquidación. El documento debe contener al menos dos ocurrencias de 'Razón Social:'.");
        }

        String razonSocial = matcher.group(1)
                .replaceAll("\\s+", " ")
                .trim();

        if (razonSocial.isEmpty()) {
            throw new LpgParseException(
                    "La Razón Social del comprador está vacía en la liquidación. Verificá que el PDF no esté cortado o mal generado.");
        }

        return razonSocial;
    }


}
