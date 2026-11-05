package com.skillbridge;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository studentRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody RegistrationRequest request) {

        Map<String, String> response = new HashMap<>();

        if (request == null
                || request.name() == null || request.name().isBlank()
                || request.email() == null || request.email().isBlank()
                || request.password() == null
                || request.password().length() < 8) {

            response.put("message",
                    "Please provide a name, email and password of at least 8 characters.");

            return ResponseEntity.badRequest().body(response);
        }

        String email = request.email().trim().toLowerCase();

        if (studentRepository.existsByEmail(email)) {
            response.put("message", "Email already registered!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        Student student = new Student();
        student.setName(request.name().trim());
        student.setEmail(email);
        student.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        studentRepository.save(student);

        response.put("message", "Registration successful!");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        Map<String, String> response = new HashMap<>();

        if (request == null
                || request.email() == null
                || request.email().isBlank()
                || request.password() == null) {

            response.put("message", "Email and password are required.");
            return ResponseEntity.badRequest().body(response);
        }

        String email = request.email().trim().toLowerCase();

        var studentOptional = studentRepository.findByEmail(email);

        if (studentOptional.isEmpty()) {
            response.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Student student = studentOptional.get();

        if (!passwordEncoder.matches(
                request.password(), student.getPasswordHash())) {

            response.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // End any existing session and create a fresh one
        HttpSession oldSession = httpRequest.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("studentId", student.getId());
        session.setMaxInactiveInterval(30 * 60);

        response.put("message", "Login successful!");
        response.put("name", student.getName());

        return ResponseEntity.ok(response);
    }

    // CURRENT STUDENT
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> currentStudent(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("studentId") instanceof Long)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        Long studentId = (Long) session.getAttribute("studentId");

        var studentOptional = studentRepository.findById(studentId);

        if (studentOptional.isEmpty()) {
            session.invalidate();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Student not found."));
        }

        Student student = studentOptional.get();

        return ResponseEntity.ok(Map.of(
                "name", student.getName(),
                "email", student.getEmail()
        ));
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully!")
        );
    }

    public record LoginRequest(
            String email,
            String password
    ) {}


    public record RegistrationRequest(
            String name,
            String email,
            String password
    ) {}
}