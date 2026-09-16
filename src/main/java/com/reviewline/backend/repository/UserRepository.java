package com.reviewline.backend.repository;

import com.reviewline.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    Optional<User> findByVerificationToken(String verificationToken);
}