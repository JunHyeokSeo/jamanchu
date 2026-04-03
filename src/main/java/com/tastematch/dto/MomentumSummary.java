package com.tastematch.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MomentumSummary {
    private String title;
    private String body;
    private String actionLabel;
    private String actionHref;
}
