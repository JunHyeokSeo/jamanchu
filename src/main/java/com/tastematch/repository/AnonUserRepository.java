package com.tastematch.repository;

import com.tastematch.domain.AnonUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnonUserRepository extends JpaRepository<AnonUser, Long> {
    Optional<AnonUser> findBySessionToken(String sessionToken);
}
