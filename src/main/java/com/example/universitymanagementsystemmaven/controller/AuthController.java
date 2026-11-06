
package com.example.universitymanagementsystemmaven.controller;

import com.example.universitymanagementsystemmaven.dto.LoginRequest;
import com.example.universitymanagementsystemmaven.dto.LoginResponse;
import com.example.universitymanagementsystemmaven.service.AuthenticationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(
            AuthenticationService authenticationService) {

        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authenticationService.login(
                request.username(),
                request.password()
        );

        LoginResponse response = new LoginResponse(
                token,
                "Bearer"
        );

        return ResponseEntity.ok(response);
    }
}
