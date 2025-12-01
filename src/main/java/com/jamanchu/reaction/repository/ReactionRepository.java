package com.jamanchu.reaction.repository;

import com.jamanchu.reaction.entity.Reaction;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<@NonNull Reaction, @NonNull Long> {
    Optional<Reaction> findByUser_IdAndPost_Id(Long userId, Long postId);

    void deleteByUser_IdAndPost_Id(Long userId, Long postId);
}
