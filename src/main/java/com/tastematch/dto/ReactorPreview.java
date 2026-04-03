package com.tastematch.dto;

import com.tastematch.domain.ReactionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReactorPreview {
    private String nickname;
    private ReactionType reactionType;
    private String recentPostCategory;
    private String recentPostSnippet;
}
