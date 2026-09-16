package com.reviewline.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardStatsResponse {
    private long reviewsThisMonth;
    private long issuesFound;
    private long securityIssues;
    private int avgScore;
}
