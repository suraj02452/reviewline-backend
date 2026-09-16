package com.reviewline.backend.security;

import com.reviewline.backend.entity.User;
import com.reviewline.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String googleId = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        User user = findOrCreateUser(googleId, email, name, picture);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        String redirectUrl = "http://localhost:5173/oauth2/callback?token=" + token;
        response.sendRedirect(redirectUrl);
    }

    private User findOrCreateUser(String googleId, String email, String name, String picture) {
        return userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User existingByEmail = userRepository.findByEmail(email).orElse(null);

                    if (existingByEmail != null) {
                        existingByEmail.setGoogleId(googleId);
                        existingByEmail.setGoogleConnected(true);
                        if (existingByEmail.getAvatarUrl() == null) {
                            existingByEmail.setAvatarUrl(picture);
                        }
                        return userRepository.save(existingByEmail);
                    }

                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .googleId(googleId)
                            .googleConnected(true)
                            .avatarUrl(picture)
                            .build();

                    return userRepository.save(newUser);
                });
    }
}