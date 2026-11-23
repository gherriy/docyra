package com.docyra.backend.auth;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.docyra.backend.scansession.ScanSessionService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
        private final ScanSessionService scanSessionService;


    public AuthController(AuthService authService, ScanSessionService scanSessionService) {
        this.authService = authService;
        this.scanSessionService = scanSessionService;

    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
        @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        scanSessionService.deactivateAll(jwt.getSubject());
    }
}