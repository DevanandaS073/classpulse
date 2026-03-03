package com.classpulse.repository;

import com.classpulse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findBySchoolClassId(Long classId);
    List<Student> findBySchoolClassIdOrderByRollNumberAsc(Long classId);
}
