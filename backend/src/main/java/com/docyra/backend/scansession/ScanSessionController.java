package com.docyra.backend.scansession;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan-sessions")
public class ScanSessionController {

    private final ScanSessionService scanSessionService;

    public ScanSessionController(ScanSessionService scanSessionService) {
        this.scanSessionService = scanSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScanSessionResponse create(@AuthenticationPrincipal Jwt jwt) {
        return scanSessionService.create(jwt.getSubject());
    }
}