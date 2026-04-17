package com.procurement.service;

import java.math.BigDecimal;

public interface EmailService {
    void sendTenderApprovalEmail(String toEmail, String tenderNo, String tenderTitle, String approvedBy);
    void sendTenderRejectionEmail(String toEmail, String tenderNo, String tenderTitle, String reason);
    void sendBidSubmissionConfirmation(String toEmail, String tenderNo, String vendorName);
    void sendContractAwardEmail(String toEmail, String tenderNo, String vendorName, BigDecimal amount);

    void sendTenderResultEmail(String toEmail, String tenderNo, String tenderTitle, String message);
}