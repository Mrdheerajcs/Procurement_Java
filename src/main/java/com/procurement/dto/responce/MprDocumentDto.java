package com.procurement.dto.responce;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MprDocumentDto {
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
}