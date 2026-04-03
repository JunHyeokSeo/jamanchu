package com.tastematch.service;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.Post;
import com.tastematch.domain.Reaction;
import com.tastematch.domain.ReactionType;
import com.tastematch.repository.PostRepository;
import com.tastematch.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;

    @Transactional
    public void toggleReaction(AnonUser reactor, Long postId, ReactionType type) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        Optional<Reaction> existing = reactionRepository.findByPostAndReactor(post, reactor);

        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getType() == type) {
                // Same reaction: remove it
                reactionRepository.delete(reaction);
            } else {
                // Different reaction: change it
                reaction.setType(type);
                reactionRepository.save(reaction);
            }
        } else {
            // No existing reaction: create one
            Reaction reaction = Reaction.builder()
                    .post(post)
                    .reactor(reactor)
                    .type(type)
                    .build();
            reactionRepository.save(reaction);
        }
    }

    @Transactional(readOnly = true)
    public Map<ReactionType, Long> getReactionsForPost(Post post) {
        Map<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        for (ReactionType type : ReactionType.values()) {
            counts.put(type, reactionRepository.countByPostAndType(post, type));
        }
        return counts;
    }
}
