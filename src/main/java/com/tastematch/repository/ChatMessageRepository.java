package com.tastematch.repository;

import com.tastematch.domain.ChatMessage;
import com.tastematch.domain.TasteMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByTasteMatchOrderByCreatedAtAsc(TasteMatch tasteMatch);
    long countByTasteMatchAndCreatedAtAfter(TasteMatch tasteMatch, LocalDateTime after);
}
