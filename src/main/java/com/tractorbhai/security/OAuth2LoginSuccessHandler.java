package com.tractorbhai.security;

import com.tractorbhai.model.User;
import com.tractorbhai.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes());
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).map(existingUser -> {
            // Update profile image if it's missing or changed
            if (picture != null) {
                existingUser.setProfileImage(picture);
            }
            if (name != null) {
                existingUser.setName(name);
            }
            return userRepository.save(existingUser);
        }).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword(""); // OAuth users don't need a local password
            newUser.setProfileImage(picture);
            return userRepository.save(newUser);
        });

        String token = jwtUtil.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth-callback")
                .queryParam("token", token)
                .queryParam("name", user.getName())
                .queryParam("email", user.getEmail())
                .queryParam("picture", user.getProfileImage())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
