package com.docyra.backend.scansession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanSessionService {

    static final Duration VALIDITY = Duration.ofHours(8);

    private final ScanSessionRepository repository;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final String scanBaseUrl;


    public ScanSessionService(ScanSessionRepository repository, Clock clock, @Value("${docyra.scan.base-url}") String scanBaseUrl) {
        this.repository = repository;
        this.clock = clock;
        this.scanBaseUrl = scanBaseUrl;
    }

    @Transactional
    public ScanSessionResponse create(String email) {
        repository.deactivateAll(email);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = clock.instant().plus(VALIDITY);

        repository.save(new ScanSession(hash(token), email, expiresAt));
        return new ScanSessionResponse(token, scanBaseUrl + "/scan?token=" +token, expiresAt);
    }

    @Transactional
    public void deactivateAll(String email) {
        repository.deactivateAll(email);
    }

    public boolean isValid(String token) {
        Instant now = clock.instant();
        return repository.findByTokenHash(hash(token))
                .map(session -> session.isUsableAt(now))
                .orElse(false);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}