package com.tastematch.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long id;
    private String senderNickname;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
}
