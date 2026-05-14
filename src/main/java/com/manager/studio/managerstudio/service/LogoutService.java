package com.manager.studio.managerstudio.service;

import com.manager.studio.managerstudio.repository.TokenUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final TokenUserRepository tokenRepository;
    private final JwtService jwtService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String jwt = authHeader.substring(7);
        String jti = jwtService.extractJti(jwt);

        tokenRepository.findById(jti).ifPresent(token -> {
            token.setRevoked(true);
            tokenRepository.save(token);
        });

        // 2. XÓA HTTPONLY COOKIE (Bước cực kỳ quan trọng)
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0); // Set thời gian sống bằng 0 để trình duyệt xóa ngay lập tức
        response.addCookie(cookie);
        SecurityContextHolder.clearContext();
    }
}
