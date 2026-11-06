
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.entity.UserAccount;
import com.example.universitymanagementsystemmaven.repository.UserAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    private final String issuer;
    private final long expirationMinutes;

    public AuthenticationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}")
            long expirationMinutes) {

        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional(readOnly = true)
    public String login(String username, String password) {

        if (username == null || password == null) {
            throw invalidCredentials();
        }

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        UserAccount account = userAccountRepository
                .findByUsername(normalizedUsername)
                .orElseThrow(this::invalidCredentials);

        if (!account.isEnabled()) {
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(
                password, account.getPasswordHash())) {
            throw invalidCredentials();
        }

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(account.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(
                        expirationMinutes,
                        ChronoUnit.MINUTES))
                .id(UUID.randomUUID().toString())
                .claim("role", account.getRole().name())
                .claim("username", account.getUsername())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }
}
