package com.procurement.dto.responce;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MprTypeDto {
    private Long typeId;
    private String typeCode;
    private String typeName;
    private String status;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime lastUpdatedDt;
}