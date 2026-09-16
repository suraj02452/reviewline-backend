package com.reviewline.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private String stripeCustomerId;

    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlanStatus planStatus = PlanStatus.FREE;

    public enum PlanStatus {
        FREE, PRO, PAST_DUE, CANCELED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Nullable because a user who signs up with Google has no password
    @Column(name = "password_hash")
    private String passwordHash;

    private String avatarUrl;

    @Column(nullable = false)
    private boolean googleConnected;

    private String googleId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    private String verificationToken;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}