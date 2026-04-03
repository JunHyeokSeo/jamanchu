package com.tastematch.service;

import com.tastematch.domain.*;
import com.tastematch.dto.MatchResponse;
import com.tastematch.repository.ChatMessageRepository;
import com.tastematch.repository.PostRepository;
import com.tastematch.repository.ReactionRepository;
import com.tastematch.repository.TasteMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final TasteMatchRepository tasteMatchRepository;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public void calculateMatches(AnonUser user) {
        // Get posts where this user reacted with SAME_HERE or EMPATHY
        List<Reaction> myReactions = reactionRepository.findByReactor(user).stream()
                .filter(r -> r.getType() == ReactionType.SAME_HERE || r.getType() == ReactionType.EMPATHY)
                .toList();

        if (myReactions.isEmpty()) return;

        List<Post> myReactedPosts = myReactions.stream()
                .map(Reaction::getPost)
                .collect(Collectors.toList());

        // Find all reactions by others on the same posts
        List<Reaction> othersReactions = reactionRepository.findByPostIn(myReactedPosts).stream()
                .filter(r -> !r.getReactor().getId().equals(user.getId()))
                .filter(r -> r.getType() == ReactionType.SAME_HERE || r.getType() == ReactionType.EMPATHY)
                .toList();

        // Count overlap per user
        Set<Long> myPostIds = myReactedPosts.stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        Map<AnonUser, Long> scoreMap = new HashMap<>();
        for (Reaction r : othersReactions) {
            if (myPostIds.contains(r.getPost().getId())) {
                scoreMap.merge(r.getReactor(), 1L, Long::sum);
            }
        }

        // Create matches for users with score >= 2 if not already exists
        for (Map.Entry<AnonUser, Long> entry : scoreMap.entrySet()) {
            AnonUser other = entry.getKey();
            int score = entry.getValue().intValue();

            if (score < 2) continue;

            // Prevent self-matching
            if (other.getId().equals(user.getId())) continue;

            // Check if match already exists in either direction
            boolean exists = tasteMatchRepository.findByUser1AndUser2(user, other).isPresent()
                    || tasteMatchRepository.findByUser1AndUser2(other, user).isPresent();

            if (!exists) {
                TasteMatch match = TasteMatch.builder()
                        .user1(user)
                        .user2(other)
                        .score(score)
                        .status(MatchStatus.PENDING)
                        .build();
                tasteMatchRepository.save(match);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getMyMatches(AnonUser user) {
        List<TasteMatch> matches = tasteMatchRepository.findByUser1OrUser2(user, user);

        return matches.stream().map(match -> {
            AnonUser partner = match.getUser1().getId().equals(user.getId())
                    ? match.getUser2()
                    : match.getUser1();

            String lastMessage = null;
            long unreadCount = 0;

            if (match.getStatus() == MatchStatus.ACCEPTED) {
                List<ChatMessage> messages = chatMessageRepository
                        .findByTasteMatchOrderByCreatedAtAsc(match);
                if (!messages.isEmpty()) {
                    lastMessage = messages.get(messages.size() - 1).getContent();
                }
                unreadCount = messages.stream()
                        .filter(m -> !m.getSender().getId().equals(user.getId()))
                        .count();
            }

            // Calculate common categories between user and partner
            List<String> commonCategories = findCommonCategories(user, partner);

            return MatchResponse.builder()
                    .id(match.getId())
                    .partnerNickname(partner.getNickname())
                    .score(match.getScore())
                    .status(match.getStatus())
                    .lastMessage(lastMessage)
                    .unreadCount(unreadCount)
                    .commonCategories(commonCategories)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void acceptMatch(AnonUser user, Long matchId) {
        TasteMatch match = tasteMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        verifyUserInMatch(user, match);
        match.setStatus(MatchStatus.ACCEPTED);
        tasteMatchRepository.save(match);
    }

    @Transactional
    public void declineMatch(AnonUser user, Long matchId) {
        TasteMatch match = tasteMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        verifyUserInMatch(user, match);
        match.setStatus(MatchStatus.DECLINED);
        tasteMatchRepository.save(match);
    }

    private List<String> findCommonCategories(AnonUser user, AnonUser partner) {
        Set<String> userCategories = reactionRepository.findByReactor(user).stream()
                .filter(r -> r.getType() == ReactionType.SAME_HERE || r.getType() == ReactionType.EMPATHY)
                .map(r -> r.getPost().getCategory())
                .collect(Collectors.toSet());

        Set<String> partnerCategories = reactionRepository.findByReactor(partner).stream()
                .filter(r -> r.getType() == ReactionType.SAME_HERE || r.getType() == ReactionType.EMPATHY)
                .map(r -> r.getPost().getCategory())
                .collect(Collectors.toSet());

        userCategories.retainAll(partnerCategories);
        return new ArrayList<>(userCategories);
    }

    private void verifyUserInMatch(AnonUser user, TasteMatch match) {
        boolean belongs = match.getUser1().getId().equals(user.getId())
                || match.getUser2().getId().equals(user.getId());
        if (!belongs) {
            throw new IllegalArgumentException("User does not belong to this match");
        }
    }
}
