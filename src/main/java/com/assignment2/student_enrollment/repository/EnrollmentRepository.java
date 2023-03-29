package com.assignment2.student_enrollment.repository;
import com.assignment2.student_enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Integer> {
}
