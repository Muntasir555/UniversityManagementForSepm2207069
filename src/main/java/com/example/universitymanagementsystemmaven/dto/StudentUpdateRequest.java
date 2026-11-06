
package com.example.universitymanagementsystemmaven.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StudentUpdateRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "Department is required")
        String department,

        @NotNull(message = "Semester is required")
        @Positive(message = "Semester must be positive")
        Integer semester

) {}
