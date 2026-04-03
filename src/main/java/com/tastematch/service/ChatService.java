package com.tastematch.service;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.ChatMessage;
import com.tastematch.domain.MatchStatus;
import com.tastematch.domain.TasteMatch;
import com.tastematch.repository.ChatMessageRepository;
import com.tastematch.repository.TasteMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final TasteMatchRepository tasteMatchRepository;

    @Transactional
    public ChatMessage sendMessage(AnonUser sender, Long matchId, String content) {
        TasteMatch match = getMatchForChat(sender, matchId);

        ChatMessage message = ChatMessage.builder()
                .tasteMatch(match)
                .sender(sender)
                .content(content)
                .build();

        return chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long matchId) {
        TasteMatch match = tasteMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));
        return chatMessageRepository.findByTasteMatchOrderByCreatedAtAsc(match);
    }

    @Transactional(readOnly = true)
    public TasteMatch getMatchForChat(AnonUser user, Long matchId) {
        TasteMatch match = tasteMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        boolean belongs = match.getUser1().getId().equals(user.getId())
                || match.getUser2().getId().equals(user.getId());
        if (!belongs) {
            throw new IllegalArgumentException("User does not belong to this match");
        }

        if (match.getStatus() != MatchStatus.ACCEPTED) {
            throw new IllegalStateException("Match is not accepted");
        }

        return match;
    }
}
