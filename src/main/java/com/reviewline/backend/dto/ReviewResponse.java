package com.reviewline.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private String id;
    private LocalDateTime createdAt;
    private String language;
    private String fileName;
    private String summary;
    private int score;
    private String code;
    private List<ReviewIssueResponse> issues;
}