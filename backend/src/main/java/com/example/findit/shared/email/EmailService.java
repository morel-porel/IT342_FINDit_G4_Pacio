package com.example.findit.shared.email;

import com.example.findit.feature.item.entity.Item;
import com.example.findit.feature.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
    }

    // ─────────────────────────────────────────────
    // 1. Welcome email — sent on registration
    // ─────────────────────────────────────────────
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to FINDit – Campus Lost & Found";
        String body = String.format("""
                Hi %s,
                
                Welcome to FINDit – the CIT-U campus lost and found platform!
                
                You can now:
                  • Report lost or found items
                  • Browse the item feed
                  • Submit claims for found items
                
                Visit: http://localhost:5173/dashboard
                
                – The FINDit Team
                """, user.getFullName());

        send(user.getEmail(), subject, body, "WELCOME", user.getId());
    }

    // ─────────────────────────────────────────────
    // 2. Claim approved — sent when admin approves
    // ─────────────────────────────────────────────
    public void sendClaimApprovedEmail(User claimant, Item item) {
        String subject = "Your claim has been APPROVED – FINDit";
        String body = String.format("""
                Hi %s,
                
                Great news! Your claim for "%s" has been approved by an administrator.
                
                Item:     %s
                Location: %s
                
                Please coordinate with the Lost & Found office to collect your item.
                
                – The FINDit Team
                """,
                claimant.getFullName(),
                item.getName(),
                item.getName(),
                item.getLocation());

        send(claimant.getEmail(), subject, body, "CLAIM_APPROVED", item.getId());
    }

    // ─────────────────────────────────────────────
    // 3. Claim rejected — sent when admin rejects
    // ─────────────────────────────────────────────
    public void sendClaimRejectedEmail(User claimant, Item item) {
        String subject = "Your claim has been reviewed – FINDit";
        String body = String.format("""
                Hi %s,
                
                Unfortunately, your claim for "%s" was not approved at this time.
                
                The item remains available and other claimants may still apply.
                If you believe this is an error, please contact the Lost & Found office with additional proof.
                
                – The FINDit Team
                """,
                claimant.getFullName(),
                item.getName());

        send(claimant.getEmail(), subject, body, "CLAIM_REJECTED", item.getId());
    }

    // ─────────────────────────────────────────────
    // Internal send — logs every attempt
    // ─────────────────────────────────────────────
    private void send(String to, String subject, String body, String type, Long referenceId) {
        String status = "SENT";
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            status = "FAILED";
            System.err.println("[EmailService] Failed to send " + type + " email to " + to + ": " + e.getMessage());
        } finally {
            logEmail(to, subject, type, referenceId, status);
        }
    }

    private void logEmail(String recipient, String subject, String type, Long referenceId, String status) {
        try {
            EmailLog log = new EmailLog();
            log.setRecipient(recipient);
            log.setSubject(subject);
            log.setType(type);
            log.setReferenceId(referenceId);
            log.setSentAt(LocalDateTime.now());
            log.setStatus(status);
            emailLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("[EmailService] Could not persist email log: " + e.getMessage());
        }
    }
}
