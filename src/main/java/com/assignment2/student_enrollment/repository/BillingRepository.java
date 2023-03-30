package com.assignment2.student_enrollment.repository;

import com.assignment2.student_enrollment.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Integer> {
    Billing findByapplicationNo(int appNum);
}
