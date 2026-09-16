package com.reviewline.backend.controller;

import com.reviewline.backend.dto.CheckoutSessionResponse;
import com.reviewline.backend.service.BillingService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutSessionResponse> createCheckout() throws StripeException {
        String userId = getCurrentUserId();
        CheckoutSessionResponse response = billingService.createCheckoutSession(userId);
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }

    @PostMapping("/portal")
    public ResponseEntity<CheckoutSessionResponse> createPortal() throws StripeException {
        String userId = getCurrentUserId();
        String portalUrl = billingService.createPortalSession(userId);
        return ResponseEntity.ok(CheckoutSessionResponse.builder().checkoutUrl(portalUrl).build());
    }
}