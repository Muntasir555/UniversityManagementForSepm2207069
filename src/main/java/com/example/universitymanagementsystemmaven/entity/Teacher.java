
package com.example.universitymanagementsystemmaven.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "teachers")
public class Teacher {

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

    @NotBlank
    @Column(nullable = false)
    private String department;

    private String designation;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_account_id",
            nullable = false,
            unique = true
    )
    private UserAccount userAccount;

    // Required by JPA
    protected Teacher() {
    }

    // Constructor
    public Teacher(
            String fullName,
            String email,
            String department,
            String designation,
            UserAccount userAccount) {

        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.designation = designation;
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

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
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

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
