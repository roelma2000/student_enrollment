package com.assignment2.student_enrollment.repository;
import com.assignment2.student_enrollment.model.Enrollment;
import com.assignment2.student_enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Integer> {
    List<Enrollment> findByStudent(Student student);
}
