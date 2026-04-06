package com.example.findit.security;

import com.example.findit.entity.User;
import com.example.findit.factory.UserFactory;
import com.example.findit.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final UserFactory userFactory;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository, TokenProvider tokenProvider, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.userFactory = userFactory;

    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String oauthId = oAuth2User.getAttribute("sub");

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Link OAuth if not already linked
            if (user.getOauthProvider() == null) {
                user.setOauthProvider("google");
                user.setOauthId(oauthId);
                userRepository.save(user);
            }
        } else {
            // Create new account
            user = userFactory.createOAuthUser(name, email, "google", oauthId);
            userRepository.save(user);
        }

        String token = tokenProvider.createToken(user);

        // Redirect to frontend with token
        String redirectUrl = "http://localhost:5173/oauth2/callback?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}