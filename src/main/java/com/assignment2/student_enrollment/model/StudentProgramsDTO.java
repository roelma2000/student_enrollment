package com.assignment2.student_enrollment.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StudentProgramsDTO {
    private String programName;
    private LocalDate startDate;
    private String duration;
    private String status;
    private String receiptNo;

    public StudentProgramsDTO(String programName, LocalDate startDate, String duration, String status, String receiptNo) {
        this.programName = programName;
        this.startDate = startDate;
        this.duration = duration;
        this.status = status;
        this.receiptNo = receiptNo;
    }
}

