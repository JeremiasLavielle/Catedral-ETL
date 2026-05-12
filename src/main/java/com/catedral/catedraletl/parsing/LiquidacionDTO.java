package com.catedral.catedraletl.parsing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionDTO {
    private String coe;
    private String fecha;
    private String cuitComprador;
    private String razonSocialComprador;
    private BigDecimal subtotalBruto;
    private List<DeduccionDTO> deducciones;
}