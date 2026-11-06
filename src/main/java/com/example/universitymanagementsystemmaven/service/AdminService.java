
package com.example.universitymanagementsystemmaven.service;

import com.example.universitymanagementsystemmaven.entity.Admin;
import com.example.universitymanagementsystemmaven.entity.Role;
import com.example.universitymanagementsystemmaven.entity.UserAccount;
import com.example.universitymanagementsystemmaven.repository.AdminRepository;
import com.example.universitymanagementsystemmaven.repository.UserAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public AdminService(
            AdminRepository adminRepository,
            UserAccountRepository userAccountRepository,
            UserAccountService userAccountService) {

        this.adminRepository = adminRepository;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    // CREATE ADMIN
    @Transactional
    public Admin createAdmin(
            String fullName,
            String email,
            String username,
            String password) {

        validateProfile(fullName, email);

        if (adminRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Admin email already exists"
            );
        }

        UserAccount account = userAccountService.createAccount(
                username,
                password,
                Role.ADMIN
        );

        Admin admin = new Admin(
                fullName,
                email,
                account
        );

        return adminRepository.save(admin);
    }

    // GET ALL ADMINS
    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    // GET ADMIN BY ID
    @Transactional(readOnly = true)
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));
    }

    // UPDATE ADMIN
    @Transactional
    public Admin updateAdmin(
            Long id,
            String fullName,
            String email) {

        Admin admin = getAdminById(id);

        validateProfile(fullName, email);

        if (!admin.getEmail().equals(email)
                && adminRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Admin email already exists"
            );
        }

        admin.setFullName(fullName);
        admin.setEmail(email);

        return adminRepository.save(admin);
    }

    // DELETE ADMIN
    @Transactional
    public void deleteAdmin(Long id) {

        Admin admin = getAdminById(id);

        Long accountId = admin.getUserAccount().getId();

        adminRepository.delete(admin);
        adminRepository.flush();

        userAccountRepository.deleteById(accountId);
    }

    // VALIDATE ADMIN PROFILE
    private void validateProfile(String fullName, String email) {

        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Full name cannot be empty"
            );
        }

        if (email == null || email.isBlank()
                || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email address"
            );
        }
    }
}

