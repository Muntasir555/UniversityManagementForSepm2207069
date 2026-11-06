
package com.example.universitymanagementsystemmaven.repository;

import com.example.universitymanagementsystemmaven.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository
        extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUserAccount_Id(Long userAccountId);

    boolean existsByEmail(String email);
}
