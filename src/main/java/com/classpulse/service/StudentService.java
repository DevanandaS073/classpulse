package com.classpulse.service;

import com.classpulse.model.SchoolClass;
import com.classpulse.model.Student;
import com.classpulse.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getStudentsByClass(Long classId) {
        return studentRepository.findBySchoolClassIdOrderByRollNumberAsc(classId);
    }

    public Student addStudent(Student student, SchoolClass schoolClass) {
        student.setSchoolClass(schoolClass);
        return studentRepository.save(student);
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
