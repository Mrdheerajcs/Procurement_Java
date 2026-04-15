package com.procurement.service.impl;

import com.procurement.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendTenderApprovalEmail(String toEmail, String tenderNo, String tenderTitle, String approvedBy) {
        try {
            Context context = new Context();
            context.setVariable("tenderNo", tenderNo);
            context.setVariable("tenderTitle", tenderTitle);
            context.setVariable("approvedBy", approvedBy);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/tender-approved", context);
            sendHtmlEmail(toEmail, "Tender Approved - " + tenderNo, htmlContent);
            log.info("Tender approval email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send tender approval email: {}", e.getMessage());
        }
    }

    @Override
    public void sendTenderRejectionEmail(String toEmail, String tenderNo, String tenderTitle, String reason) {
        try {
            Context context = new Context();
            context.setVariable("tenderNo", tenderNo);
            context.setVariable("tenderTitle", tenderTitle);
            context.setVariable("reason", reason);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/tender-rejected", context);
            sendHtmlEmail(toEmail, "Tender Rejected - " + tenderNo, htmlContent);
            log.info("Tender rejection email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send tender rejection email: {}", e.getMessage());
        }
    }

    @Override
    public void sendBidSubmissionConfirmation(String toEmail, String tenderNo, String vendorName) {
        try {
            Context context = new Context();
            context.setVariable("tenderNo", tenderNo);
            context.setVariable("vendorName", vendorName);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/bid-submitted", context);
            sendHtmlEmail(toEmail, "Bid Submitted Successfully - " + tenderNo, htmlContent);
            log.info("Bid submission confirmation sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send bid submission email: {}", e.getMessage());
        }
    }

    @Override
    public void sendContractAwardEmail(String toEmail, String tenderNo, String vendorName, BigDecimal amount) {
        try {
            Context context = new Context();
            context.setVariable("tenderNo", tenderNo);
            context.setVariable("vendorName", vendorName);
            context.setVariable("amount", amount);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/contract-awarded", context);
            sendHtmlEmail(toEmail, "Contract Awarded - " + tenderNo, htmlContent);
            log.info("Contract award email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send contract award email: {}", e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }
}