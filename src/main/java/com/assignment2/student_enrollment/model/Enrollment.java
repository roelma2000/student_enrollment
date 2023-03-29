package com.assignment2.student_enrollment.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity  //for creating the table
@Table(name = "tblenrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generating auto inc
    private int applicationNo;
    private  int studentId;
    private  int programCode;
    private LocalDate startDate;
    private  double amountPaid;
    private String status;
}
