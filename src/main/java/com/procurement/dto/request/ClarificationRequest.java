package com.procurement.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClarificationRequest {
    private Long bidTechnicalId;
    private String question;
    private LocalDateTime deadline;  // When vendor must respond
}