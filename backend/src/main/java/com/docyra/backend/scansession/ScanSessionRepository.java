package com.docyra.backend.scansession;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ScanSessionRepository extends JpaRepository<ScanSession, Long> {

    Optional<ScanSession> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update ScanSession s set s.active = false where s.createdBy = :email and s.active = true")
    int deactivateAll(String email);
}