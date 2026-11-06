
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.entity.Role;
import com.example.universitymanagementsystemmaven.entity.UserAccount;
import com.example.universitymanagementsystemmaven.repository.UserAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {

        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount createAccount(
            String username,
            String rawPassword,
            Role role) {

        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username cannot be empty"
            );
        }

        if (rawPassword == null || rawPassword.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters"
            );
        }

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        if (userAccountRepository.existsByUsername(
                normalizedUsername)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        String hashedPassword =
                passwordEncoder.encode(rawPassword);

        UserAccount account = new UserAccount(
                normalizedUsername,
                hashedPassword,
                role
        );

        return userAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByUsername(String username) {

        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        return userAccountRepository
                .findByUsername(normalizedUsername);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {

        if (username == null || username.isBlank()) {
            return false;
        }

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        return userAccountRepository
                .existsByUsername(normalizedUsername);
    }
}
