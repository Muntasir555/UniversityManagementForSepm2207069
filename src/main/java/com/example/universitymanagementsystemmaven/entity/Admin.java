
package com.example.universitymanagementsystemmaven.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_account_id",
            nullable = false,
            unique = true
    )
    private UserAccount userAccount;

    // Required by JPA
    protected Admin() {
    }

    // Constructor
    public Admin(
            String fullName,
            String email,
            UserAccount userAccount) {
        this.fullName = fullName;
        this.email = email;
        this.userAccount = userAccount;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
