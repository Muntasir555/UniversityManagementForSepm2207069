
package com.example.universitymanagementsystemmaven.repository;

import com.example.universitymanagementsystemmaven.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Optional<Student> findByUserAccount_Id(Long userAccountId);

    boolean existsByEmail(String email);
}
