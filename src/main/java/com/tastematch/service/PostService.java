package com.tastematch.service;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.Post;
import com.tastematch.domain.Reaction;
import com.tastematch.domain.ReactionType;
import com.tastematch.dto.PostRequest;
import com.tastematch.dto.PostResponse;
import com.tastematch.dto.ReactorPreview;
import com.tastematch.repository.PostRepository;
import com.tastematch.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;

    @Transactional
    public Post createPost(AnonUser author, PostRequest req) {
        Post post = Post.builder()
                .author(author)
                .content(req.getContent())
                .category(req.getCategory())
                .build();
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByCategory(String category) {
        return postRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Post> getMyPosts(AnonUser user) {
        return postRepository.findByAuthor(user);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostResponse(Post post, AnonUser currentUser) {
        List<Reaction> reactions = reactionRepository.findByPost(post);

        Map<ReactionType, Long> reactionCounts = new EnumMap<>(ReactionType.class);
        for (ReactionType type : ReactionType.values()) {
            reactionCounts.put(type, 0L);
        }
        for (Reaction r : reactions) {
            reactionCounts.merge(r.getType(), 1L, Long::sum);
        }

        ReactionType myReaction = null;
        if (currentUser != null) {
            myReaction = reactions.stream()
                    .filter(r -> r.getReactor().getId().equals(currentUser.getId()))
                    .map(Reaction::getType)
                    .findFirst()
                    .orElse(null);
        }

        long totalReactions = reactions.size();
        List<ReactorPreview> reactorPreviews = isOwnedBy(post, currentUser)
                ? buildReactorPreviews(post, reactions, currentUser)
                : List.of();

        return PostResponse.builder()
                .id(post.getId())
                .nickname(post.getAuthor().getNickname())
                .content(post.getContent())
                .category(post.getCategory())
                .createdAt(post.getCreatedAt())
                .reactions(reactionCounts)
                .myReaction(myReaction)
                .reactionCount(totalReactions)
                .reactorPreviews(reactorPreviews)
                .build();
    }

    private boolean isOwnedBy(Post post, AnonUser currentUser) {
        return currentUser != null && post.getAuthor().getId().equals(currentUser.getId());
    }

    private List<ReactorPreview> buildReactorPreviews(Post post, List<Reaction> reactions, AnonUser currentUser) {
        return reactions.stream()
                .filter(reaction -> reaction.getType() == ReactionType.EMPATHY || reaction.getType() == ReactionType.SAME_HERE)
                .filter(reaction -> !reaction.getReactor().getId().equals(currentUser.getId()))
                .sorted(Comparator.comparing(Reaction::getCreatedAt).reversed())
                .limit(3)
                .map(reaction -> toReactorPreview(reaction.getReactor(), reaction.getType()))
                .collect(Collectors.toList());
    }

    private ReactorPreview toReactorPreview(AnonUser reactor, ReactionType reactionType) {
        List<Post> recentPosts = postRepository.findTop2ByAuthorOrderByCreatedAtDesc(reactor);
        Optional<Post> previewPost = recentPosts.stream().findFirst();

        return ReactorPreview.builder()
                .nickname(reactor.getNickname())
                .reactionType(reactionType)
                .recentPostCategory(previewPost.map(Post::getCategory).orElse(null))
                .recentPostSnippet(previewPost
                        .map(Post::getContent)
                        .map(this::truncateSnippet)
                        .orElse(null))
                .build();
    }

    private String truncateSnippet(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 60 ? normalized : normalized.substring(0, 57) + "...";
    }
}
