package com.manager.studio.managerstudio.service;

import com.manager.studio.managerstudio.entity.TokenEntity;
import com.manager.studio.managerstudio.entity.UserEntity;
import com.manager.studio.managerstudio.entity.UserRoleEntity;
import com.manager.studio.managerstudio.exception.BusinessException;
import com.manager.studio.managerstudio.repository.RoleRepository;
import com.manager.studio.managerstudio.repository.TokenUserRepository;
import com.manager.studio.managerstudio.repository.UserRepository;
import com.manager.studio.managerstudio.repository.UserRoleRepository;
import com.manager.studio.managerstudio.request.auth.AuthenticationResponse;
import com.manager.studio.managerstudio.request.auth.LoginRequest;
import com.manager.studio.managerstudio.request.auth.RegisterRequest;
import com.manager.studio.managerstudio.util.DeviceUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenUserRepository tokenUserRepository;

    @Transactional
    public UserEntity register(RegisterRequest request) {
        // 1. Tạo User mới
        var user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        var savedUser = userRepository.save(user);

        // 2. Gán Role mặc định (ví dụ: ROLE_USER)
        var defaultRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        var userRole = UserRoleEntity.builder()
                .user(savedUser)
                .role(defaultRole)
                .build();

        userRoleRepository.save(userRole);


        return savedUser;
    }

    public AuthenticationResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) throws BusinessException {
        // 1. Xác thực
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Tìm user (Nên dùng EntityGraph để tránh LazyInitializationException)
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        // 3. Tạo Token
        boolean isMobile = DeviceUtils.isMobile(httpRequest);
        long accessExpiration = isMobile ? 1_000_000L : 500_000L;
        long refreshExpiration = accessExpiration + 604_800_000L; // Refresh nên dài hơn (VD: 7 ngày)

        String jwtToken = jwtService.generateToken(user, accessExpiration);
        String refreshToken = jwtService.generateRefreshToken(user, refreshExpiration);

        // 4. Lưu vào DB
        TokenEntity tokenEntity = TokenEntity.builder()
                .jti(jwtService.extractJti(jwtToken))
                .refreshJti(jwtService.extractJti(refreshToken)) // Lấy đúng từ refreshToken
                .user(user)
                .revoked(false)
                .expiryDate(convertToLocalDateTime(jwtService.extractExpiration(jwtToken)))
                .build();
        tokenUserRepository.save(tokenEntity);

        // 5. Thiết lập HttpOnly Cookie cho Refresh Token
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Chỉ chạy trên HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshExpiration / 1000));
        response.addCookie(cookie);

        // 6. Chỉ trả Access Token trong Body
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        // 1. Kiểm tra Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        final String jwt = authHeader.substring(7);

        // 2. Trích xuất JTI từ Token
        String jti = jwtService.extractJti(jwt);

        // 3. Tìm và thu hồi Token trong Database
        tokenUserRepository.findById(jti).ifPresent(token -> {
            token.setRevoked(true);
            tokenUserRepository.save(token);
        });

        // 4. Xóa Context của Spring Security
        SecurityContextHolder.clearContext();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        String refreshJti = jwtService.extractJti(refreshToken);

        var session = tokenUserRepository.findByRefreshJti(refreshJti)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.isRevoked()) {
            // Nếu dùng Rotation, bạn có thể thêm logic:
            // Nếu một RT đã bị revoked mà vẫn có người dùng lại -> Có dấu hiệu bị hack -> Thu hồi toàn bộ session của user này.
            throw new RuntimeException("Token has been revoked");
        }

        // 1. Tạo Access Token mới
        String newAccessToken = jwtService.generateToken(session.getUser(), 1800_000L);

        // 2. Tạo Refresh Token mới (Xoay vòng)
        String newRefreshToken = jwtService.generateRefreshToken(session.getUser(), 604_800_000L);

        // 3. Cập nhật Session với cả 2 JTI mới
        session.setJti(jwtService.extractJti(newAccessToken));
        session.setRefreshJti(jwtService.extractJti(newRefreshToken)); // Cập nhật JTI mới của Refresh Token
        session.setExpiryDate(convertToLocalDateTime(jwtService.extractExpiration(newAccessToken)));

        tokenUserRepository.save(session);

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken) // Trả về Refresh Token mới
                .build();
    }
}