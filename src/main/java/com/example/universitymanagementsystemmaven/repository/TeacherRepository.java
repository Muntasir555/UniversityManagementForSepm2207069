
package com.example.universitymanagementsystemmaven.repository;

import com.example.universitymanagementsystemmaven.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository
        extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserAccount_Id(Long userAccountId);

    boolean existsByEmail(String email);
}
