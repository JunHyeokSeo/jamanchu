package com.jamanchu.user.repository;

import com.jamanchu.user.entity.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<@NonNull User, UUID> {
    Optional<User> findByEmailAndIsDeletedIsFalse(String email);
}
