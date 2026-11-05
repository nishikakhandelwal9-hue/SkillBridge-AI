package com.skillbridge;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/api")
public class InternshipController {

    private final StudentRepository studentRepository;

    public InternshipController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Existing feature: search internships using manually entered skills
    @GetMapping("/internships")
    public List<Map<String, Object>> getInternships(
            @RequestParam(defaultValue = "") String skills) {

        return findMatchingInternships(skills);
    }

    // New feature: recommend internships using saved student profile
    @GetMapping("/internships/recommended")
    public ResponseEntity<?> getRecommendedInternships(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("studentId") instanceof Long)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        Long studentId = (Long) session.getAttribute("studentId");

        Optional<Student> studentOptional =
                studentRepository.findById(studentId);

        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found."));
        }

        Student student = studentOptional.get();

        String skills = student.getSkills();

        if (skills == null || skills.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Please complete your profile and add your skills first."
                    ));
        }

        return ResponseEntity.ok(findMatchingInternships(skills));
    }

    // Shared internship matching algorithm
    private List<Map<String, Object>> findMatchingInternships(
            String skills) {

        List<Map<String, Object>> results = new ArrayList<>();

        Set<String> studentSkills = new HashSet<>();

        for (String skill : skills.split(",")) {
            studentSkills.add(skill.trim().toLowerCase());
        }

        String[][] internships = {
            {"Java Backend Developer", "Java,SQL,Spring Boot"},
            {"Frontend Developer", "HTML,CSS,JavaScript"},
            {"Data Analyst", "Python,SQL,Excel"},
            {"Software Developer", "Java,DSA,Git"},
            {"AI/ML Intern", "Python,Machine Learning,TensorFlow"},
            {"Full Stack Developer", "Java,HTML,CSS,JavaScript"}
        };

        for (String[] internship : internships) {

            String[] requiredSkills = internship[1].split(",");

            int matched = 0;

            List<String> missingSkills = new ArrayList<>();

            for (String required : requiredSkills) {

                if (studentSkills.contains(required.trim().toLowerCase())) {
                    matched++;
                } else {
                    missingSkills.add(required.trim());
                }
            }

            int percentage = (int) Math.round(
                    matched * 100.0 / requiredSkills.length
            );

            Map<String, Object> result = new LinkedHashMap<>();

            result.put("role", internship[0]);
            result.put("requiredSkills", internship[1]);
            result.put("matchPercentage", percentage);
            result.put("missingSkills", missingSkills);

            results.add(result);
        }

        results.sort((a, b) ->
                Integer.compare(
                        (int) b.get("matchPercentage"),
                        (int) a.get("matchPercentage")
                )
        );

        return results;
    }
}