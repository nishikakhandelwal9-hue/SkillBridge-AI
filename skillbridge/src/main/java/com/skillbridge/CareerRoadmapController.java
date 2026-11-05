package com.skillbridge;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/api/roadmap")
public class CareerRoadmapController {

    private final StudentRepository studentRepository;

    public CareerRoadmapController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public ResponseEntity<?> getRoadmap(HttpServletRequest request) {

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

        String careerGoal = student.getCareerGoal();
        String skills = student.getSkills();

        if (careerGoal == null || careerGoal.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Please add your career goal in your profile."));
        }

        Map<String, List<String>> careerPaths = new LinkedHashMap<>();

        careerPaths.put("software developer",
                List.of("Java", "DSA", "SQL", "Git", "Spring Boot"));

        careerPaths.put("frontend developer",
                List.of("HTML", "CSS", "JavaScript", "React", "Git"));

        careerPaths.put("data analyst",
                List.of("Excel", "SQL", "Python", "Power BI", "Statistics"));

        careerPaths.put("ai/ml engineer",
                List.of("Python", "Statistics", "Machine Learning",
                        "TensorFlow", "Deep Learning"));

        careerPaths.put("full stack developer",
                List.of("HTML", "CSS", "JavaScript", "React",
                        "Java", "Spring Boot", "SQL"));

        String normalizedGoal = careerGoal.trim().toLowerCase(Locale.ROOT);

        List<String> requiredSkills = careerPaths.get(normalizedGoal);

        if (requiredSkills == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Career goal not supported yet. Choose Software Developer, Frontend Developer, Data Analyst, AI/ML Engineer, or Full Stack Developer."));
        }

        Set<String> studentSkills = new HashSet<>();

        if (skills != null) {
            for (String skill : skills.split(",")) {
                studentSkills.add(skill.trim().toLowerCase(Locale.ROOT));
            }
        }

        List<Map<String, Object>> roadmap = new ArrayList<>();
        int completed = 0;

        for (int i = 0; i < requiredSkills.size(); i++) {

            String skill = requiredSkills.get(i);

            boolean alreadyKnown =
                    studentSkills.contains(skill.toLowerCase(Locale.ROOT));

            if (alreadyKnown) {
                completed++;
            }

            Map<String, Object> step = new LinkedHashMap<>();

            step.put("step", i + 1);
            step.put("skill", skill);
            step.put("status", alreadyKnown ? "Completed" : "To Learn");

            roadmap.add(step);
        }

        int progress = (int) Math.round(
                completed * 100.0 / requiredSkills.size()
        );

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("careerGoal", careerGoal);
        response.put("progress", progress);
        response.put("roadmap", roadmap);

        return ResponseEntity.ok(response);
    }
}