package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Role;
import com.janajagoran.scms.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
