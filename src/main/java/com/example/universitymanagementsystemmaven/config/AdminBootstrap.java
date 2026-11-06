
package com.example.universitymanagementsystemmaven.config;

import com.example.universitymanagementsystemmaven.repository.AdminRepository;
import com.example.universitymanagementsystemmaven.service.AdminService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
public class AdminBootstrap {

    @Bean
    @ConditionalOnProperty(
            name = "app.bootstrap.admin.enabled",
            havingValue = "true"
    )
    public ApplicationRunner createInitialAdmin(
            AdminRepository adminRepository,
            AdminService adminService,

            @Value("${BOOTSTRAP_ADMIN_USERNAME:}")
            String username,

            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
            String password,

            @Value("${BOOTSTRAP_ADMIN_EMAIL:}")
            String email) {

        return args -> {

            if (adminRepository.count() > 0) {
                throw new IllegalStateException(
                        "Admin already exists. Disable bootstrap."
                );
            }

            if (username.isBlank()
                    || password.length() < 12
                    || email.isBlank()) {

                throw new IllegalStateException(
                        "Valid bootstrap credentials are required."
                );
            }

            adminService.createAdmin(
                    "System Administrator",
                    email,
                    username,
                    password
            );

            System.out.println(
                    "Initial administrator created successfully."
            );
        };
    }
}
