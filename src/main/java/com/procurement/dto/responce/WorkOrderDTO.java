package com.procurement.dto.responce;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class WorkOrderDTO {
    private String workOrderNo;
    private String contractNo;
    private String tenderTitle;
    private String vendorName;
    private LocalDate issueDate;
    private LocalDate deliveryDate;
    private BigDecimal totalAmount;
    private String status;
    private String pdfPath;
}