package com.docyra.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountInitializer implements ApplicationRunner {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AccountInitializer(AppUserRepository repository, PasswordEncoder passwordEncoder,
                              @Value("${docyra.account.email}") String email,
                              @Value("${docyra.account.password}") String password) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase();
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repository.existsByEmail(email)) {
            repository.save(new AppUser(email, passwordEncoder.encode(password)));
        }
    }
}