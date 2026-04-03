package com.tastematch.repository;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthor(AnonUser author);
    List<Post> findTop2ByAuthorOrderByCreatedAtDesc(AnonUser author);
    List<Post> findByCategory(String category);
}
