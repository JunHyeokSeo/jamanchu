package com.tastematch.dto;

import com.tastematch.domain.ReactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String nickname;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private Map<ReactionType, Long> reactions;
    private ReactionType myReaction;
    private long reactionCount;
    private List<ReactorPreview> reactorPreviews;
}
