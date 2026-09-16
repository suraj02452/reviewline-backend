package com.reviewline.backend.service;

import com.reviewline.backend.dto.CheckoutSessionResponse;
import com.reviewline.backend.entity.User;
import com.reviewline.backend.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final UserRepository userRepository;

    @Value("${stripe.price.pro-monthly}")
    private String proPriceId;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    public CheckoutSessionResponse createCheckoutSession(String userId) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String customerId = getOrCreateStripeCustomer(user);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(proPriceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build();

        Session session = Session.create(params);

        return CheckoutSessionResponse.builder()
                .checkoutUrl(session.getUrl())
                .build();
    }

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getName())
                .build();

        Customer customer = Customer.create(params);

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        return customer.getId();
    }

    public void handleWebhookEvent(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> {
                // Ignore event types we don't care about
            }
        }
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Could not deserialize checkout session"));

        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();

        if (customerId == null) return;

        userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
            user.setStripeSubscriptionId(subscriptionId);
            user.setPlanStatus(User.PlanStatus.PRO);
            userRepository.save(user);
        });
    }

    private void handleSubscriptionUpdated(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Could not deserialize subscription"));

        String customerId = subscription.getCustomer();
        String status = subscription.getStatus();

        if (customerId == null) return;

        userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
            user.setPlanStatus(mapStripeStatus(status));
            userRepository.save(user);
        });
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("Could not deserialize subscription"));

        String customerId = subscription.getCustomer();

        if (customerId == null) return;

        userRepository.findByStripeCustomerId(customerId).ifPresent(user -> {
            user.setPlanStatus(User.PlanStatus.FREE);
            user.setStripeSubscriptionId(null);
            userRepository.save(user);
        });
    }

    private User.PlanStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active", "trialing" -> User.PlanStatus.PRO;
            case "past_due" -> User.PlanStatus.PAST_DUE;
            default -> User.PlanStatus.CANCELED;
        };
    }

    public String createPortalSession(String userId) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStripeCustomerId() == null) {
            throw new IllegalArgumentException("No billing account found for this user");
        }

        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(user.getStripeCustomerId())
                        .setReturnUrl(successUrl)
                        .build();

        com.stripe.model.billingportal.Session session =
                com.stripe.model.billingportal.Session.create(params);

        return session.getUrl();
    }
}