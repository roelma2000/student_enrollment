package com.assignment2.student_enrollment.controller;
import com.assignment2.student_enrollment.model.StudentProgramsDTO;
import com.assignment2.student_enrollment.model.Student;
import com.assignment2.student_enrollment.repository.StudentRepository;
import com.assignment2.student_enrollment.model.Programs;
import com.assignment2.student_enrollment.repository.ProgramsRepository;
import com.assignment2.student_enrollment.model.Enrollment;
import com.assignment2.student_enrollment.repository.EnrollmentRepository;
import com.assignment2.student_enrollment.model.Billing;
import com.assignment2.student_enrollment.repository.BillingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


@Controller
public class StudentController {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProgramsRepository programsRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private BillingRepository billingRepository;
    /**
     * Code for index page - registration login
     */
    @GetMapping({"/", "/index"})
    public String index(Model model, Principal principal) {
        if (principal != null) {
            Student currentUser = studentRepository.findByUserName(principal.getName());
            model.addAttribute("currentUser", currentUser);
        }
        return "index";
    }


    @RequestMapping({"/", "/login"})
    public String getLoginPage(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("authenticationError") != null) {
            model.addAttribute("authenticationError", request.getSession().getAttribute("authenticationError"));
            request.getSession().removeAttribute("authenticationError");
        }
        return "index";
    }


    /**
     * Code for enrollment page
     */
    @GetMapping("/enrollment")
    public String enrollmentPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Student currentUser = studentRepository.findByUserName(auth.getName());
        model.addAttribute("currentUser", currentUser);
        // Add any other necessary attributes or data to the model
        List<Programs> programs = programsRepository.findAll();
        model.addAttribute("programs", programs);
        return "enrollment";
    }

    @PostMapping("/process-enrollment")
    public String processEnrollment(@RequestParam("programCode") int programCode, @ModelAttribute("billing") Billing billing, BindingResult bindingResult, Model model, Principal principal) {
        // Get the current user
        Student currentUser = studentRepository.findByUserName(principal.getName());

        // Get the selected program
        Programs selectedProgram = programsRepository.findById(programCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid program code: " + programCode));
        //validate

        if(bindingResult.hasErrors()){
            List<ObjectError> errors = bindingResult.getAllErrors();
            model.addAttribute("errors", errors);
            model.addAttribute("selectedProgram", selectedProgram);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("billing", billing);
            return "checkout";
        }else{
            // Calculate the total amount including 13% tax
            double amountPaid = selectedProgram.getFee() + (selectedProgram.getFee() * 0.13);

            // Create a new enrollment object
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(currentUser);
            Optional<Programs> optionalProgram = programsRepository.findById(programCode);
            if (optionalProgram.isPresent()) {
                Programs program = optionalProgram.get();
                enrollment.setProgram(program);
            }
            enrollment.setAmountPaid(amountPaid);
            enrollment.setStatus("Paid");
            enrollment.setStartDate(selectedProgram.getStartDate()); // Set the startDate from the selected program

            // Save the enrollment
//            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            // Generate 18 random numbers and 2 random letters for receiptno
            String receiptno = generateRandomReceiptNo();
            billing.setReceiptno(receiptno);

            // Set the reference to the saved Enrollment object
            billing.setEnrollment(enrollment);

            // Save the billing object in the database
            billingRepository.save(billing);


            // Retrieve the saved enrollment
            Enrollment savedEnrollment = billing.getEnrollment();

            model.addAttribute("enrollment", savedEnrollment);
            model.addAttribute("selectedProgram", selectedProgram);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("billing", billing);

            return "payment-confirmation";
        }
    }


    /**
     * Code for Registration page
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model, Principal principal) {
        if (principal != null) {
            Student currentUser = studentRepository.findByUserName(principal.getName());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("student", currentUser);
        } else {
            model.addAttribute("student", new Student());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(@Valid Student student, BindingResult bindingResult, Model model, Principal principal){
        //Username validation
        if(student.getUserName().isBlank()){
            bindingResult.addError(new FieldError("student","userName","Username should not be blank."));
        }
        //Password validation
        if(student.getPassword().isBlank()){
            bindingResult.addError(new FieldError("student","password","Password should not be blank."));
        }
        //firstname
        if(student.getFirstname().isBlank()){
            bindingResult.addError(new FieldError("student","firstname","Firstname should not be blank."));
        }
        //lastname
        if(student.getLastname().isBlank()){
            bindingResult.addError(new FieldError("student","lastname","Lastname should not be blank."));
        }
        //address
        if(student.getAddress().isBlank()){
            bindingResult.addError(new FieldError("student","address","Address should not be blank."));
        }
        //city
        if(student.getCity().isBlank()){
            bindingResult.addError(new FieldError("student","city","City should not be blank."));
        }
        // postalCode
        if(student.getPostalCode().isBlank() || student.getPostalCode().length() > 7){
            bindingResult.addError(new FieldError("student","postalCode","postalCode should not be blank."));
        }


        if(bindingResult.hasErrors()){
            return "register";
        }

        if (principal != null) {
            // If the user is authenticated, update their account
            Student currentUser = studentRepository.findByUserName(principal.getName());
            if (currentUser != null) {
                student.setStudentId(currentUser.getStudentId());
            }
        }

        studentRepository.save(student);

        if (principal != null) {
            // If the user is authenticated, redirect to the home page after updating their account
            return "redirect:/";
        } else {
            // If the user is not authenticated, redirect to the login page after creating their account
            return "redirect:/login";
        }
    }

/**
 * Code for checkout page
 */
@PostMapping("/checkout")
public String showCheckoutPage(@RequestParam("programCode") int programCode, Model model, Principal principal) {
    // Get the current user
    Student currentUser = studentRepository.findByUserName(principal.getName());

    // Get the selected program
    Programs selectedProgram = programsRepository.findById(programCode)
            .orElseThrow(() -> new IllegalArgumentException("Invalid program code: " + programCode));

    model.addAttribute("currentUser", currentUser);
    model.addAttribute("program", selectedProgram);
    model.addAttribute("billing", new Billing());

    return "checkout";
}

    private String generateRandomReceiptNo() {
        // Generate 18 random digits
        String digits = new Random().ints(18, 0, 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining());

        // Generate 2 random uppercase letters
        String letters = new Random().ints(2, 'A', 'Z' + 1)
                .mapToObj(i -> (char) i)
                .map(String::valueOf)
                .collect(Collectors.joining());

        return digits + letters;
    }
/**
 * Code for Programs page
 */
@GetMapping("/programs")
public String showProgramsPage(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Student currentUser = studentRepository.findByUserName(auth.getName());
    List<Enrollment> enrollments = enrollmentRepository.findByStudent(currentUser);
    List<StudentProgramsDTO> studentPrograms = new ArrayList<>();

    for (Enrollment enrollment: enrollments){
        Programs program = enrollment.getProgram();
        int appNum = enrollment.getApplicationNo();
        Billing billing = billingRepository.findByapplicationNo(appNum);

        StudentProgramsDTO studentProgram = new StudentProgramsDTO(
                program.getProgramName(),
                program.getStartDate(),
                program.getDuration(),
                enrollment.getStatus(),
                billing.getReceiptno()
        );

        studentPrograms.add(studentProgram);
    }

    model.addAttribute("currentUser", currentUser);
    model.addAttribute("studentPrograms", studentPrograms);

    return "programs";
}


}
