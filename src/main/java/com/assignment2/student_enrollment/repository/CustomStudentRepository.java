package com.assignment2.student_enrollment.repository;
import com.assignment2.student_enrollment.model.StudentProgramsDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomStudentRepository {
    @Query("SELECT new com.assignment2.student_enrollment.model.StudentProgramsDTO(p.programName, p.startDate, p.duration, e.status, b.receiptno) " +
            "FROM Enrollment e " +
            "JOIN e.student u " +
            "JOIN e.program p " +
            "JOIN e.billing b " +
            "WHERE u.userName = :userId")
    List<StudentProgramsDTO> findEnrolledProgramsByUserId(@Param("userId") String userId);
}

