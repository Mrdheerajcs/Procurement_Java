package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_entity_type", columnList = "entityType"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;  // CREATE, UPDATE, DELETE, APPROVE, REJECT, PUBLISH, LOGIN, LOGOUT

    @Column(nullable = false)
    private String entityType;  // MprHeader, MprDetail, TenderHeader, Vendor, User, etc.

    private Long entityId;

    @Column(length = 2000)
    private String oldValue;

    @Column(length = 2000)
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String ipAddress;

    private String userAgent;

    @Column(length = 500)
    private String remarks;
}