
package com.example.universitymanagementsystemmaven.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeacherCreateRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,

        @NotBlank(message = "Department is required")
        String department,

        String designation,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                message = "Password must be at least 8 characters"
        )
        String password

) {}
