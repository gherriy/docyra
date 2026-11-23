package com.docyra.backend.scansession;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ScanTokenFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Scan-Token";

    private final ScanSessionService scanSessionService;

    public ScanTokenFilter(ScanSessionService scanSessionService) {
        this.scanSessionService = scanSessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader(HEADER);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        if (!scanSessionService.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"status":401,"detail":"Le QR code a expiré. Scannez à nouveau le QR code sur l'ordinateur."}""");
            return;
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "scan", null, List.of(new SimpleGrantedAuthority("SCAN"))));
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }
}