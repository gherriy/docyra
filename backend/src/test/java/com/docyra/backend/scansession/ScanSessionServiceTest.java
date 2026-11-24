package com.docyra.backend.scansession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ScanSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-23T10:00:00Z");

    private final ScanSessionRepository repository = mock(ScanSessionRepository.class);
    private final ScanSessionService service =
            new ScanSessionService(repository, Clock.fixed(NOW, ZoneOffset.UTC), "http://192.168.1.20:4200");

    private void givenSession(String token, ScanSession session) {
        when(repository.findByTokenHash(ScanSessionService.hash(token))).thenReturn(Optional.of(session));
    }

    @Test
    void acceptsValidToken() {
        givenSession("abc", new ScanSession(ScanSessionService.hash("abc"), "s@docyra.local", NOW.plusSeconds(60)));

        assertThat(service.isValid("abc")).isTrue();
    }

    @Test
    void rejectsExpiredToken() {
        givenSession("abc", new ScanSession(ScanSessionService.hash("abc"), "s@docyra.local", NOW.minusSeconds(60)));

        assertThat(service.isValid("abc")).isFalse();
    }

    @Test
    void rejectsDeactivatedToken() {
        ScanSession session = new ScanSession(ScanSessionService.hash("abc"), "s@docyra.local", NOW.plusSeconds(60));
        session.deactivate();
        givenSession("abc", session);

        assertThat(service.isValid("abc")).isFalse();
    }

    @Test
    void rejectsUnknownToken() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThat(service.isValid("inconnu")).isFalse();
    }

    @Test
    void newTokenDeactivatesPreviousOnesAndStoresOnlyTheHash() {
        
        ScanSessionResponse response = service.create("s@docyra.local");

        verify(repository).deactivateAll("s@docyra.local");
        assertThat(response.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(8)));

        ArgumentCaptor<ScanSession> saved = ArgumentCaptor.forClass(ScanSession.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue()).extracting("tokenHash").isEqualTo(ScanSessionService.hash(response.token()));
        assertThat(response.scanUrl()).isEqualTo("http://192.168.1.20:4200/scan?token=" + response.token());

    }
}