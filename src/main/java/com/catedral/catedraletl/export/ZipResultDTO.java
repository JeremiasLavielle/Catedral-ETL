package com.catedral.catedraletl.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZipResultDTO {
    private byte[] zipBytes;
    private String fileName;
}
