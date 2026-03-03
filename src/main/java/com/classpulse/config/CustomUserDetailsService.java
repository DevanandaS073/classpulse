package com.classpulse.config;

import com.classpulse.model.Teacher;
import com.classpulse.repository.TeacherRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;

    public CustomUserDetailsService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found: " + email));

        return User.builder()
                .username(teacher.getEmail())
                .password(teacher.getPassword())
                .roles("TEACHER")
                .build();
    }
}
