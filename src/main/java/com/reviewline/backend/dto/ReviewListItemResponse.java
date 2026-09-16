package com.reviewline.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReviewListItemResponse {
    private String id;
    private LocalDateTime createdAt;
    private String language;
    private String fileName;
    private int issueCount;
    private int score;
    private String summary;
}