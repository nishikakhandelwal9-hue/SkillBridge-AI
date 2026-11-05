package com.skillbridge;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        // Check student login
        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("studentId") instanceof Long)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        // Validate uploaded file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please upload a PDF resume."));
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only PDF files are supported."));
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Maximum file size is 5 MB."));
        }

        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {

            String resumeText = new PDFTextStripper()
                    .getText(document)
                    .toLowerCase(Locale.ROOT);

            List<String> knownSkills = Arrays.asList(
                    "java", "python", "sql", "html", "css",
                    "javascript", "spring boot", "git",
                    "react", "machine learning", "tensorflow",
                    "excel", "dsa", "c++"
            );

            List<String> detectedSkills = new ArrayList<>();

            for (String skill : knownSkills) {

                String pattern = "(?<![a-z0-9+#])"
                        + java.util.regex.Pattern.quote(skill)
                        + "(?![a-z0-9+#])";

                if (java.util.regex.Pattern
                        .compile(pattern)
                        .matcher(resumeText)
                        .find()) {

                    detectedSkills.add(skill);
                }
            }

            String[][] internships = {
                    {"Java Backend Developer", "java,sql,spring boot"},
                    {"Frontend Developer", "html,css,javascript"},
                    {"Data Analyst", "python,sql,excel"},
                    {"Software Developer", "java,dsa,git"},
                    {"AI/ML Intern", "python,machine learning,tensorflow"},
                    {"Full Stack Developer", "java,html,css,javascript"}
            };

            List<Map<String, Object>> matches = new ArrayList<>();

            for (String[] internship : internships) {

                String[] requiredSkills = internship[1].split(",");

                int matched = 0;
                List<String> missingSkills = new ArrayList<>();

                for (String required : requiredSkills) {

                    if (detectedSkills.contains(required)) {
                        matched++;
                    } else {
                        missingSkills.add(required);
                    }
                }

                int percentage = (int) Math.round(
                        matched * 100.0 / requiredSkills.length
                );

                Map<String, Object> result = new LinkedHashMap<>();

                result.put("role", internship[0]);
                result.put("matchPercentage", percentage);
                result.put("missingSkills", missingSkills);

                matches.add(result);
            }

            matches.sort((a, b) ->
                    Integer.compare(
                            (int) b.get("matchPercentage"),
                            (int) a.get("matchPercentage")
                    )
            );

            Map<String, Object> response = new LinkedHashMap<>();

            response.put("message", "Resume analyzed successfully!");
            response.put("detectedSkills", detectedSkills);
            response.put("totalSkills", detectedSkills.size());
            response.put("recommendations", matches);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Unable to read PDF. Please upload a valid text-based PDF."
                    ));
        }
    }
}