package com.catedral.catedraletl.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TxtResultDTO {
    private String periodo;
    private String cabecera;
    private String detalle;
}
