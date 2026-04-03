package com.procurement.dto.responce;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StateDTO {
    private Long stateId;
    private String stateName;
    private String stateCode;
    private Long countryId;   // 🔹 Foreign Key (Country)
    private LocalDateTime createdAt;
}
