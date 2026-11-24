package com.docyra.backend.scansession;

import java.time.Instant;

public record ScanSessionResponse(String token, String scanUrl,Instant expiresAt) {
}