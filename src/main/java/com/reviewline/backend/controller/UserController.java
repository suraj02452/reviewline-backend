package com.reviewline.backend.controller;

import com.reviewline.backend.dto.UserResponse;
import com.reviewline.backend.entity.User;
import com.reviewline.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.reviewline.backend.dto.UpdatePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.reviewline.backend.repository.ReviewRepository;
import com.reviewline.backend.entity.Review;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long reviewsThisMonth = reviewRepository.countByUserIdSince(userId, startOfMonth);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .googleConnected(user.isGoogleConnected())
                .createdAt(user.getCreatedAt())
                .planStatus(user.getPlanStatus().name().toLowerCase())
                .reviewsThisMonth((int) reviewsThisMonth)
                .build();

        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        String userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentUser() {
        String userId = getCurrentUserId();

        List<Review> userReviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        reviewRepository.deleteAll(userReviews);

        userRepository.deleteById(userId);

        return ResponseEntity.ok().build();
    }
}