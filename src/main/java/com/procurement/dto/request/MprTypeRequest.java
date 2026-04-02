package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MprTypeRequest {
    private Long typeId;
    private String typeCode;
    private String typeName;
}