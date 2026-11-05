package com.skillbridge;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;
    private String college;

private String branch;

private String academicYear;

private String skills;

private String careerGoal;

    public Student() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        }
 public String getCollege() {
    return college;
}

public void setCollege(String college) {
    this.college = college;
}

public String getBranch() {
    return branch;
}

public void setBranch(String branch) {
    this.branch = branch;
}

public String getAcademicYear() {
    return academicYear;
}

public void setAcademicYear(String academicYear) {
    this.academicYear = academicYear;
}

public String getSkills() {
    return skills;
}

public void setSkills(String skills) {
    this.skills = skills;
}

public String getCareerGoal() {
    return careerGoal;
}

public void setCareerGoal(String careerGoal) {
    this.careerGoal = careerGoal;
}
}