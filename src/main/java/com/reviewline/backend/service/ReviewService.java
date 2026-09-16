package com.reviewline.backend.service;

import com.reviewline.backend.dto.*;
import com.reviewline.backend.entity.Review;
import com.reviewline.backend.entity.ReviewIssue;
import com.reviewline.backend.entity.User;
import com.reviewline.backend.repository.ReviewRepository;
import com.reviewline.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AnthropicService anthropicService;


    public ReviewResponse submitReview(String userId, SubmitReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPlanStatus() == User.PlanStatus.FREE) {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long reviewsThisMonth = reviewRepository.countByUserIdSince(userId, startOfMonth);

            if (reviewsThisMonth >= 25) {
                throw new IllegalArgumentException(
                        "Free plan limit reached (25 reviews/month). Upgrade to Pro for unlimited reviews.");
            }
        }


        JsonNode aiResult = anthropicService.getReviewFromClaude(
                request.getLanguage(), request.getCode());

        String finalLanguage = request.getLanguage().equals("auto")
                ? aiResult.path("detectedLanguage").asText(request.getLanguage())
                : request.getLanguage();

        Review review = Review.builder()
                .user(user)
                .language(finalLanguage)
                .fileName(request.getFileName())
                .code(request.getCode())
                .summary(aiResult.path("summary").asText("No summary available."))
                .score(aiResult.path("score").asInt(50))
                .build();

        for (JsonNode issueNode : aiResult.path("issues")) {
            ReviewIssue issue = ReviewIssue.builder()
                    .review(review)
                    .severity(parseSeverity(issueNode.path("severity").asText("style")))
                    .lineStart(issueNode.path("lineStart").asInt(1))
                    .lineEnd(issueNode.path("lineEnd").asInt(1))
                    .title(issueNode.path("title").asText("Untitled issue"))
                    .explanation(issueNode.path("explanation").asText(""))
                    .suggestion(nullableText(issueNode, "suggestion"))
                    .beforeCode(nullableText(issueNode, "beforeCode"))
                    .afterCode(nullableText(issueNode, "afterCode"))
                    .build();

            review.getIssues().add(issue);
        }

        Review savedReview = reviewRepository.save(review);

        return toReviewResponse(savedReview);
    }

    private ReviewIssue.Severity parseSeverity(String value) {
        try {
            return ReviewIssue.Severity.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ReviewIssue.Severity.STYLE;
        }
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return (value.isMissingNode() || value.isNull()) ? null : value.asText();
    }


    private ReviewResponse toReviewResponse(Review review) {
        List<ReviewIssueResponse> issueResponses = review.getIssues().stream()
                .map(issue -> ReviewIssueResponse.builder()
                        .id(issue.getId())
                        .severity(issue.getSeverity().name().toLowerCase())
                        .lineStart(issue.getLineStart())
                        .lineEnd(issue.getLineEnd())
                        .title(issue.getTitle())
                        .explanation(issue.getExplanation())
                        .suggestion(issue.getSuggestion())
                        .beforeCode(issue.getBeforeCode())
                        .afterCode(issue.getAfterCode())
                        .build())
                .collect(Collectors.toList());

        return ReviewResponse.builder()
                .id(review.getId())
                .createdAt(review.getCreatedAt())
                .language(review.getLanguage())
                .fileName(review.getFileName())
                .summary(review.getSummary())
                .score(review.getScore())
                .code(review.getCode())
                .issues(issueResponses)
                .build();
    }

    public List<ReviewListItemResponse> getReviewHistory(String userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return reviews.stream()
                .map(review -> ReviewListItemResponse.builder()
                        .id(review.getId())
                        .createdAt(review.getCreatedAt())
                        .language(review.getLanguage())
                        .fileName(review.getFileName())
                        .issueCount(review.getIssues().size())
                        .score(review.getScore())
                        .summary(review.getSummary())
                        .build())
                .collect(Collectors.toList());
    }

    public ReviewResponse getReviewById(String userId, String reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        return toReviewResponse(review);
    }

    public DashboardStatsResponse getDashboardStats(String userId) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long reviewsThisMonth = reviewRepository.countByUserIdSince(userId, startOfMonth);
        long issuesFound = reviewRepository.countIssuesByUserIdSince(userId, startOfMonth);
        long securityIssues = reviewRepository.countSecurityIssuesByUserIdSince(userId, startOfMonth);
        Double avgScore = reviewRepository.averageScoreByUserIdSince(userId, startOfMonth);

        return DashboardStatsResponse.builder()
                .reviewsThisMonth(reviewsThisMonth)
                .issuesFound(issuesFound)
                .securityIssues(securityIssues)
                .avgScore((int) Math.round(avgScore))
                .build();
    }
}