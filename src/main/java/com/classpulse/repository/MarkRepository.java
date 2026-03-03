package com.classpulse.repository;

import com.classpulse.model.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {
    List<Mark> findByTestId(Long testId);
    List<Mark> findByStudentId(Long studentId);
    Optional<Mark> findByStudentIdAndTestId(Long studentId, Long testId);
    List<Mark> findByTestIdIn(List<Long> testIds);
}
