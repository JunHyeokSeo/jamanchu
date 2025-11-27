package com.jamanchu.post.dto.response;

import com.jamanchu.post.entity.Post;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PostResponse(
        Long id,
        Long userId,
        String content,
        Instant create_at,
        Instant update_at
) {
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .content(post.getContent())
                .create_at(post.getCreate_at())
                .update_at(post.getUpdate_at())
                .build();
    }
}
