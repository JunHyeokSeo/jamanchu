package com.jamanchu.reaction.service;

import com.jamanchu.post.entity.Post;
import com.jamanchu.post.repository.PostRepository;
import com.jamanchu.reaction.entity.Reaction;
import com.jamanchu.reaction.entity.ReactionType;
import com.jamanchu.reaction.repository.ReactionRepository;
import com.jamanchu.user.entity.User;
import com.jamanchu.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void createReaction(Long userId, Long postId, ReactionType type) {
        User user = userRepository.findByIdAndIsDeletedIsFalse(userId).orElseThrow(() -> new IllegalArgumentException("유효한 유저가 아닙니다."));
        Post post = postRepository.findByIdAndIsDeletedIsFalse(postId).orElseThrow(() -> new IllegalArgumentException("유효한 포스트가 아닙니다."));

        Reaction reaction = Reaction.builder()
                .user(user)
                .post(post)
                .type(type)
                .build();

        reactionRepository.save(reaction);
    }

    public void deleteReaction(Long reactionId, Long userId) {
        Reaction reaction = reactionRepository.findById(reactionId).orElseThrow(() -> new IllegalArgumentException("유효한 리액션이 아닙니다."));
        User user = userRepository.findByIdAndIsDeletedIsFalse(userId).orElseThrow(() -> new IllegalArgumentException("유효한 유저가 아닙니다."));

        if (!reaction.isCreatedBy(user))
            throw new IllegalStateException("사용자가 생성한 리액션이 아닙니다.");

        reactionRepository.deleteById(reactionId);
    }

    public void updateReaction(Long reactionId, ReactionType type) {
        Reaction reaction = reactionRepository.findById(reactionId).orElseThrow(() -> new IllegalArgumentException("유효한 리액션이 아닙니다."));
        reaction.updateType(type);
    }

    //조회
}
