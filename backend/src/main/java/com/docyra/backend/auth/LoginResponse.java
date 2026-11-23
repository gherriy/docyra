package com.docyra.backend.auth;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}