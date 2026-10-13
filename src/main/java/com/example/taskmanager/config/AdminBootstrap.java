package com.example.taskmanager.config;

import com.example.taskmanager.entities.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first SysAdmin on startup when BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD are set.
 * Only a SysAdmin can create teams and change roles, so without this nobody could get started.
 * Does nothing if the variables are empty or the user already exists.
 */
@Slf4j
@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (userRepository.findUserByEmailId(email).isPresent()) {
            log.info("Bootstrap admin {} already exists, skipping", email);
            return;
        }
        User admin = User.builder()
                .userName("admin")
                .emailId(email)
                .password(passwordEncoder.encode(password))
                .build();
        // User's @PrePersist always sets role=Normal on insert, so promote it with a second save (an update).
        admin = userRepository.save(admin);
        admin.setRole(UserRole.SysAdmin);
        userRepository.save(admin);
        log.info("Created bootstrap SysAdmin {}", email);
    }
}
