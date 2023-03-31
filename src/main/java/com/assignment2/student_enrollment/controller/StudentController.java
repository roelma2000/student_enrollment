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
     * Handle index
     * - The method first checks whether the principal parameter is null.
     * - If it is not null, it uses the studentRepository to find the Student entity with the same username
     * as the authenticated user's name property. It then adds this Student entity to the Model
     * object with the key "currentUser".
     */
    @GetMapping({"/", "/index"})
    public String index(Model model, Principal principal) {
        if (principal != null) {
            Student currentUser = studentRepository.findByUserName(principal.getName());
            model.addAttribute("currentUser", currentUser);
        }
        return "index";
    }

    /**
     * Handle Login page
     * - The method first checks whether the authenticationError attribute is present in the user's session
     * by calling request.getSession().getAttribute("authenticationError"). If it is present, the value of the
     * authenticationError attribute is added to the Model object with the key "authenticationError".
     *
     * - The method then calls request to remove the authenticationError property to prevent the error message
     * from appearing on future requests.
     */
    @RequestMapping({"/", "/login"})
    public String getLoginPage(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("authenticationError") != null) {
            model.addAttribute("authenticationError", request.getSession().getAttribute("authenticationError"));
            request.getSession().removeAttribute("authenticationError");
        }
        return "index";
    }


    /**
     * Handle enrollment page
     * 1. Gets the currently authenticated user's Authentication object from the SecurityContextHolder
     * 2. Use studentRepository to find username and adds this Student entity to Model object with key "currentUser"
     * 3. Retrieves all Programs then add program to Model object with key "programs"
     * 4. Render enrollment
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

    /**
     * Handle enrollment page - Process the enrollment
     * -> @RequestParam("programCode") - parameter extracted from the request's query string.
     * -> @ModelAttribute("billing") Billing billing - specifies billing object is bound to the HTTP request's body.
     * -> BindingResult bindingResult - collect errors from the form submission.
     * -> Model model - used to add attributes to the view that will be rendered.
     * -> Principal principal - represents the currently authenticated user.
     *
     * 1. Uses the studentRepository to find the Student entity with the same username as the authenticated user's
     *          name property
     * 2. Retrieves the Programs entity with the specified programCode from the programsRepository
     * 3. If there are any validation errors, add list of errors to Model errors. Pass back models to checkout page.
     * 4. If no validation errors,
     *      a. calculate final payment
     *      b. create new Enrollment object, set properties, save via enrollmentRepository
     *      c. generate random receipt number
     *      d. save billing
     *      e. Add savedEnrollment, selectedProgram ....objects to model objects to return to view
     */
    @PostMapping("/process-enrollment")
    public String processEnrollment(@RequestParam("programCode") int programCode, @ModelAttribute("billing") Billing
            billing, BindingResult bindingResult, Model model, Principal principal) {
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
           Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            // Generate 18 random numbers and 2 random letters for receiptno
            String receiptno = generateRandomReceiptNo();
            billing.setReceiptno(receiptno);

            // Set the reference to the saved Enrollment object
            billing.setEnrollment(enrollment);

            // Save the billing object in the database
            billingRepository.save(billing);

            model.addAttribute("enrollment", savedEnrollment);
            model.addAttribute("selectedProgram", selectedProgram);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("billing", billing);

            return "payment-confirmation";
        }
    }

    /**
     * Handle checkout page
     * - Uses the studentRepository to find the Student entity with the same username
     * - Retrieves the Programs entity with the specified programCode from the programsRepository.
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

    /**
     * Generate Random Receipt Number for Billing
     */
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
     * Handle Registration page
     * - Checks whether the principal parameter is null.
     * - Uses studentRepository to find the Student entity using authenticated username property.
     * - Add Student entity to the Model object with the key "currentUser"
     * - Also adds it to the Model object with the key "student" - for Account Update
     * - If the principal parameter is null, add new Student - for Account Create
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

    /**
     * Handle Registration page
     * Parameters:
     * ---> @Valid Student student specifies that the student object is validated against its defined constraints.
     * ---> BindingResult bindingResult is used to collect errors from the form submission.
     * ---> Model model is used to add attributes to the view that will be rendered.
     * ---> Principal principal represents the currently authenticated user.
     *
     *  - checks whether the user is authenticated by checking the principal parameter
     *  -  If the user is authenticated,
     *      -- retrieves the current Student entity
     *      -- sets the studentId property of the student object to the current user's studentId to ensure
     *         that the updated Student object is associated with the correct user.
     *      -- save updated object
     *  - if authenticated, redirect to "/"
     *  - if not authenticated, redirect tp "/login" to re-login
     */
    @PostMapping("/register")
    public String registerStudent(@Valid Student student, BindingResult bindingResult, Model model, Principal principal){
        // Check if the username already exists
        Student existingStudent = studentRepository.findByUserName(student.getUserName());
        if (existingStudent != null) {
            bindingResult.addError(new FieldError("student", "userName", "Username already exists. Please choose a different username."));
        }
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
     * Handle Programs page
     *  - Gets the currently authenticated user's Authentication object
     *  - Retrieves the current Student entity
     *  - Find all Enrollment entities  by calling enrollmentRepository.findByStudent(currentUser)
     *  - Create list of StudentProgramsDTO objects
     *  - Iterates over each Enrollment object and retrieves its associated Programs and Billing objects to
     *      create a new StudentProgramsDTO object for each enrollment
     *  - Adds the currentUser and studentPrograms objects to the Model object for Programs to render
     *  - redirect to '/programs'
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
