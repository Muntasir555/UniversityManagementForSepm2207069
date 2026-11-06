
package com.example.universitymanagementsystemmaven.config;

import com.example.universitymanagementsystemmaven.entity.UserAccount;
import com.example.universitymanagementsystemmaven.repository.UserAccountRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserAccountRepository userAccountRepository;

    public SecurityConfig(
            UserAccountRepository userAccountRepository) {

        this.userAccountRepository = userAccountRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http


                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.GET,
                                "/ui",
                                "/ui/**"
                        ).permitAll()



                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()



                        .requestMatchers(
                                "/api/admins",
                                "/api/admins/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/teachers"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/teachers"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/teachers/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/teachers/**"
                        ).hasAnyRole("ADMIN", "TEACHER")


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/teachers/**"
                        ).hasAnyRole("ADMIN", "TEACHER")


                        .requestMatchers(
                                "/api/teachers",
                                "/api/teachers/**"
                        ).denyAll()


                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/students"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/students/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/**"
                        ).hasAnyRole("ADMIN", "STUDENT")


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/students/**"
                        ).hasAnyRole("ADMIN", "STUDENT")


                        .requestMatchers(
                                "/api/students",
                                "/api/students/**"
                        ).denyAll()



                        .anyRequest().denyAll()
                )



                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }



    private Converter<Jwt, ? extends AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        return jwt -> {

            Long accountId;

            // Extract account ID from verified JWT
            try {
                accountId = Long.valueOf(jwt.getSubject());
            } catch (Exception ex) {
                throw invalidToken();
            }

            // Find account in PostgreSQL
            UserAccount account = userAccountRepository
                    .findById(accountId)
                    .orElseThrow(this::invalidToken);

            // Reject disabled accounts
            if (!account.isEnabled()) {
                throw invalidToken();
            }

            String tokenRole = jwt.getClaimAsString("role");

            if (!account.getRole().name().equals(tokenRole)) {
                throw invalidToken();
            }

            var authority = new SimpleGrantedAuthority(
                    "ROLE_" + account.getRole().name()
            );

            // Return authenticated user
            return new JwtAuthenticationToken(
                    jwt,
                    List.of(authority),
                    account.getUsername()
            );
        };
    }



    private OAuth2AuthenticationException invalidToken() {

        return new OAuth2AuthenticationException(
                new OAuth2Error(
                        "invalid_token",
                        "Invalid or inactive user account",
                        null
                )
        );
    }
}
