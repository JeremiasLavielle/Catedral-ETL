package com.catedral.catedraletl.api;

import com.catedral.catedraletl.exception.GenerationException;
import com.catedral.catedraletl.exception.LpgParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LpgParseException.class)
    public ResponseEntity<Map<String, String>> handleLpgParseException(LpgParseException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Error al leer el PDF",
                        "detalle", ex.getMessage()
                ));
    }

    @ExceptionHandler(GenerationException.class)
    public ResponseEntity<Map<String, String>> handleGenerationException(GenerationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Error al generar los archivos de salida",
                        "detalle", ex.getMessage()
                ));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "El archivo enviado no pudo ser leído",
                        "detalle", "Verificá que el archivo sea un PDF válido y no esté dañado."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Error inesperado del servidor",
                        "detalle", "Ocurrió un error no esperado. Contactá al equipo de mantenimiento con el siguiente detalle: " + ex.getMessage()
                ));
    }
}
