package com.catedral.catedraletl.export;

import com.catedral.catedraletl.generation.TxtResultDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ExportService {

    public ZipResultDTO export(TxtResultDTO txtResult) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("CABECERA.txt"));
            zos.write(txtResult.getCabecera().getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("DETALLE.txt"));
            zos.write(txtResult.getDetalle().getBytes());
            zos.closeEntry();
        }

        return ZipResultDTO.builder()
                .zipBytes(baos.toByteArray())
                .fileName("liquidaciones.zip")
                .build();
    }
}