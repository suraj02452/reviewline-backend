package com.reviewline.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private boolean googleConnected;
    private LocalDateTime createdAt;
    private String planStatus;
    private int reviewsThisMonth;
}