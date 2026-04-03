package com.tastematch.dto;

import com.tastematch.domain.MatchStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MatchResponse {
    private Long id;
    private String partnerNickname;
    private int score;
    private MatchStatus status;
    private String lastMessage;
    private long unreadCount;
    private List<String> commonCategories;
}
