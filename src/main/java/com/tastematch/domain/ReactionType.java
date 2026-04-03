package com.tastematch.domain;

public enum ReactionType {
    LIKE("좋아요"),
    EMPATHY("공감"),
    SAME_HERE("나도!"),
    CURIOUS("궁금해요");

    private final String label;

    ReactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
