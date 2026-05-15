package com.example.findit.shared.email;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * EmailLog — persists every email send attempt to the email_logs table.
 */
@Entity
@Table(name = "email_logs")
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "subject")
    private String subject;

    @Column(name = "type")
    private String type;           // WELCOME / CLAIM_APPROVED / CLAIM_REJECTED

    @Column(name = "reference_id")
    private Long referenceId;      // user.id for WELCOME, item.id for claim emails

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "status")
    private String status;         // SENT / FAILED

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
