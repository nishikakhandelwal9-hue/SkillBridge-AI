package com.skillbridge;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final StudentRepository studentRepository;

    public ProfileController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // GET STUDENT PROFILE
    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("studentId") instanceof Long)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        Long studentId = (Long) session.getAttribute("studentId");

        return studentRepository.findById(studentId)
                .<ResponseEntity<?>>map(student -> {

                    Map<String, Object> profile = new HashMap<>();

                    profile.put("name", student.getName());
                    profile.put("email", student.getEmail());
                    profile.put("college", safe(student.getCollege()));
                    profile.put("branch", safe(student.getBranch()));
                    profile.put("academicYear", safe(student.getAcademicYear()));
                    profile.put("skills", safe(student.getSkills()));
                    profile.put("careerGoal", safe(student.getCareerGoal()));

                    return ResponseEntity.ok(profile);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found.")));
    }

    // UPDATE STUDENT PROFILE
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileRequest profileRequest,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("studentId") instanceof Long)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        Long studentId = (Long) session.getAttribute("studentId");

        var studentOptional = studentRepository.findById(studentId);

        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found."));
        }

        if (profileRequest == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Profile details are required."));
        }

        Student student = studentOptional.get();

        student.setCollege(safe(profileRequest.college()));
        student.setBranch(safe(profileRequest.branch()));
        student.setAcademicYear(safe(profileRequest.academicYear()));
        student.setSkills(safe(profileRequest.skills()));
        student.setCareerGoal(safe(profileRequest.careerGoal()));

        studentRepository.save(student);

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully!")
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public record ProfileRequest(
            String college,
            String branch,
            String academicYear,
            String skills,
            String careerGoal
    ) {}
}