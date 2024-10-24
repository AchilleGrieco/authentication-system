package com.oauth_login.oauth_login.authentication.repository;

import com.oauth_login.oauth_login.authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String name);
}
