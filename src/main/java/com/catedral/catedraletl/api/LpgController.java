package com.catedral.catedraletl.api;

import com.catedral.catedraletl.export.ExportService;
import com.catedral.catedraletl.export.ZipResultDTO;
import com.catedral.catedraletl.generation.GenerationService;
import com.catedral.catedraletl.generation.TxtResultDTO;
import com.catedral.catedraletl.parsing.LpgDocumentDTO;
import com.catedral.catedraletl.parsing.LpgParserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class LpgController {

    private final LpgParserService lpgParserService;
    private final GenerationService generationService;
    private final ExportService exportService;

    public LpgController(LpgParserService lpgParserService,
                         GenerationService generationService,
                         ExportService exportService) {
        this.lpgParserService = lpgParserService;
        this.generationService = generationService;
        this.exportService = exportService;
    }

    @PostMapping("/generar")
    public ResponseEntity<byte[]> generar(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] pdfBytes = file.getBytes();
        LpgDocumentDTO document = lpgParserService.parse(pdfBytes);
        TxtResultDTO txt = generationService.generate(document);
        ZipResultDTO zip = exportService.export(txt);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + zip.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip.getZipBytes());
    }
}