# Reviewline Backend

Backend service for **Reviewline**, an AI-powered code review SaaS. It exposes a REST API that authenticates users, accepts code for review, runs it through the Anthropic API to generate AI-driven review feedback, and manages subscription billing.

## Features

- **Auth** — JWT-based authentication with OAuth2 login support
- **AI code review** — submitted code is analyzed via the Anthropic API, returning structured review issues
- **Review history** — track and retrieve past reviews per user
- **Billing** — Stripe-powered subscription checkout and webhook handling
- **Dashboard stats** — usage/summary data for the frontend dashboard
- **Email notifications** — transactional emails via a dedicated email service
- **Dockerized** — ships with a Dockerfile for containerized deployment

## Tech stack

Java · Spring Boot · Spring Security · JWT · OAuth2 · Stripe API · Anthropic API · Maven · Docker

## Architecture

```
controller/   AuthController, ReviewController, BillingController, UserController, WebhookController
service/      AuthService, ReviewService, BillingService, AnthropicService, EmailService
security/     JwtAuthFilter, JwtUtil, OAuth2LoginSuccessHandler
entity/       User, Review, ReviewIssue
repository/   UserRepository, ReviewRepository
```

## Running locally

```bash
./mvnw spring-boot:run
```

Or with Docker:

```bash
docker build -t reviewline-backend .
docker run -p 8080:8080 reviewline-backend
```

Configure required environment variables (database, JWT secret, Anthropic API key, Stripe keys, OAuth2 credentials) in `src/main/resources/application.properties`.

## Related

- [reviewline-frontend](https://github.com/suraj02452/reviewline-frontend) — the React/TypeScript client for this API
