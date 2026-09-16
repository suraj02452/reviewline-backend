package com.reviewline.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewIssueResponse {
    private String id;
    private String severity;
    private int lineStart;
    private int lineEnd;
    private String title;
    private String explanation;
    private String suggestion;
    private String beforeCode;
    private String afterCode;
}