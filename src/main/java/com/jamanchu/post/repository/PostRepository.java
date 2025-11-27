package com.jamanchu.post.repository;

import com.jamanchu.post.entity.Post;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<@NonNull Post, @NonNull Long> {
    Optional<Post> findByIdAndIsDeletedIsFalse(Long id);

    List<Post> findAllByIsDeletedIsFalse(Sort sort);
}
