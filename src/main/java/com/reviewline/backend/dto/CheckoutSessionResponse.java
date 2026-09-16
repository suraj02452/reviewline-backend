package com.reviewline.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CheckoutSessionResponse {
    private String checkoutUrl;
}