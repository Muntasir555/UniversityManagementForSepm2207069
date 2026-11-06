
package com.example.universitymanagementsystemmaven.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "students")
public class Student {

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

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer semester;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_account_id",
            nullable = false,
            unique = true
    )
    private UserAccount userAccount;

    // Required by JPA
    protected Student() {
    }

    // Constructor
    public Student(
            String fullName,
            String email,
            String department,
            Integer semester,
            UserAccount userAccount) {

        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.semester = semester;
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

    public Integer getSemester() {
        return semester;
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

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
