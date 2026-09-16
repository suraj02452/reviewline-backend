package com.reviewline.backend.controller;

import com.reviewline.backend.dto.ReviewListItemResponse;
import com.reviewline.backend.dto.ReviewResponse;
import com.reviewline.backend.dto.SubmitReviewRequest;
import com.reviewline.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.reviewline.backend.dto.DashboardStatsResponse;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@Valid @RequestBody SubmitReviewRequest request) {
        String userId = getCurrentUserId();
        ReviewResponse response = reviewService.submitReview(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewListItemResponse>> getHistory() {
        String userId = getCurrentUserId();
        List<ReviewListItemResponse> history = reviewService.getReviewHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable String id) {
        String userId = getCurrentUserId();
        ReviewResponse response = reviewService.getReviewById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        String userId = getCurrentUserId();
        DashboardStatsResponse stats = reviewService.getDashboardStats(userId);
        return ResponseEntity.ok(stats);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}