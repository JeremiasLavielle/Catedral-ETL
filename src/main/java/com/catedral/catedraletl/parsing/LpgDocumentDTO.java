package com.catedral.catedraletl.parsing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LpgDocumentDTO {
    private String cuitVendedor;
    private String periodo;
    private List<LiquidacionDTO> liquidaciones;
}