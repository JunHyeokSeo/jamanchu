package com.jamanchu.post.repository;

import com.jamanchu.post.entity.Post;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<@NonNull Post, @NonNull Long> {
    Optional<Post> findByIdAndIsDeletedIsFalse(Long id);
}
