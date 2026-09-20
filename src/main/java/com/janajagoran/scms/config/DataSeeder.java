package com.janajagoran.scms.config;

import com.janajagoran.scms.entity.Role;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.RoleName;
import com.janajagoran.scms.repository.RoleRepository;
import com.janajagoran.scms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // ============================================
        // Seed Roles
        // ============================================
        for (RoleName roleName : RoleName.values()) {

            roleRepository.findByName(roleName)
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .name(roleName)
                                            .build()
                            )
                    );
        }

        // ============================================
        // Seed Default Users
        // ============================================

        createUser(
                "Super Admin",
                "superadmin@janajagoranclub.org",
                "9999999999",
                "Admin@123",
                RoleName.SUPER_ADMIN
        );

        createUser(
                "Admin",
                "admin@janajagoranclub.org",
                "6666666666",
                "Admin@123",
                RoleName.ADMIN
        );

        createUser(
                "Secretary",
                "secretary@janajagoranclub.org",
                "8888888888",
                "Secretary@123",
                RoleName.SECRETARY
        );

        createUser(
                "President",
                "president@janajagoranclub.org",
                "7777777777",
                "President@123",
                RoleName.PRESIDENT
        );
    }

    private void createUser(
            String name,
            String email,
            String phone,
            String password,
            RoleName roleName
    ) {

        // Don't create duplicate users
        if (userRepository.existsByEmail(email)) {
            return;
        }

        // Find role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found: " + roleName
                        )
                );

        // Create user
        User user = User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(role)
                .isActive(true)
                .build();

        userRepository.save(user);

    }
}