package com.reviewline.backend.repository;

import com.reviewline.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Review> findByIdAndUserId(String id, String userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId AND r.createdAt >= :startOfMonth")
    long countByUserIdSince(@Param("userId") String userId, @Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT COUNT(i) FROM ReviewIssue i WHERE i.review.user.id = :userId AND i.review.createdAt >= :startOfMonth")
    long countIssuesByUserIdSince(@Param("userId") String userId, @Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT COUNT(i) FROM ReviewIssue i WHERE i.review.user.id = :userId AND i.review.createdAt >= :startOfMonth AND i.severity = 'SECURITY'")
    long countSecurityIssuesByUserIdSince(@Param("userId") String userId, @Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.user.id = :userId AND r.createdAt >= :startOfMonth")
    Double averageScoreByUserIdSince(@Param("userId") String userId, @Param("startOfMonth") LocalDateTime startOfMonth);
}