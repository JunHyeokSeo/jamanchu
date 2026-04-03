package com.tastematch.repository;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.Post;
import com.tastematch.domain.Reaction;
import com.tastematch.domain.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByPost(Post post);
    List<Reaction> findByReactor(AnonUser reactor);
    boolean existsByPostAndReactor(Post post, AnonUser reactor);
    long countByPostAndType(Post post, ReactionType type);
    List<Reaction> findByPostIn(List<Post> posts);
    Optional<Reaction> findByPostAndReactor(Post post, AnonUser reactor);
}
