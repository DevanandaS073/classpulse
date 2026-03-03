package com.classpulse.service;

import com.classpulse.model.Teacher;
import com.classpulse.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Teacher register(Teacher teacher) {
        if (teacherRepository.existsByEmail(teacher.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        return teacherRepository.save(teacher);
    }

    public Teacher findByEmail(String email) {
        return teacherRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
    }
}
