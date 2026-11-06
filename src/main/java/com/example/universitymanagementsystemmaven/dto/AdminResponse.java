
package com.example.universitymanagementsystemmaven.dto;

import com.example.universitymanagementsystemmaven.entity.Admin;

public record AdminResponse(
        Long id,
        String fullName,
        String email,
        String username
) {

    public static AdminResponse fromEntity(Admin admin) {

        return new AdminResponse(
                admin.getId(),
                admin.getFullName(),
                admin.getEmail(),
                admin.getUserAccount().getUsername()
        );
    }
}

