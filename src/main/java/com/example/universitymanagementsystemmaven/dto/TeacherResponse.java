
package com.example.universitymanagementsystemmaven.dto;

import com.example.universitymanagementsystemmaven.entity.Teacher;

public record TeacherResponse(
        Long id,
        String fullName,
        String email,
        String department,
        String designation,
        String username
) {

    public static TeacherResponse fromEntity(Teacher teacher) {

        return new TeacherResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getDepartment(),
                teacher.getDesignation(),
                teacher.getUserAccount().getUsername()
        );
    }
}
