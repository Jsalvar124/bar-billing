package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.UserImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserImpl, UUID> {
    Optional<UserImpl> findByEmail(String username);
    boolean existsByEmail(String email);
}
