package com.docyra.backend.scansession;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "scan_session")
public class ScanSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    protected ScanSession() {
    }

    public ScanSession(String tokenHash, String createdBy, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }

    public boolean isUsableAt(Instant now) {
        return active && expiresAt.isAfter(now);
    }

    public void deactivate() {
        this.active = false;
    }
}