package com.manager.studio.managerstudio.config;


import com.manager.studio.managerstudio.exception.BusinessException;
import com.manager.studio.managerstudio.repository.TokenUserRepository;
import com.manager.studio.managerstudio.service.JwtService;
import com.manager.studio.managerstudio.util.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
@Component
//@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenUserRepository tokenRepository;

    // Inject resolver để đẩy Exception sang @RestControllerAdvice
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            TokenUserRepository tokenRepository,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver // Chỉ định đúng bean
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
        this.resolver = resolver;
    }
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);

            // Các hàm extract này có thể ném ra ExpiredJwtException hoặc MalformedJwtException
            final String username = jwtService.extractUsername(jwt);
            final String jti = jwtService.extractJti(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 1. Kiểm tra tính hợp lệ kỹ thuật (Hạn dùng, chữ ký)
                // Nếu JwtService ném BusinessException (401), nó sẽ được catch ở khối try-catch bên ngoài
                boolean isJwtValid = jwtService.isTokenValid(jwt, userDetails);

                // 2. Kiểm tra trạng thái Session trong DB
                boolean isSessionActive = tokenRepository.findByJti(jti)
                        .map(t -> !t.isRevoked())
                        .orElse(false);

                if (isJwtValid && isSessionActive) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Nếu session bị thu hồi, ném lỗi 401 để Client biết cần login lại
                    throw new BusinessException("Phiên đăng nhập không hợp lệ hoặc đã bị thu hồi", HttpStatus.UNAUTHORIZED);
                }
            }
            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            // Đẩy sang GlobalExceptionHandler để trả về ErrorResponse JSON
            resolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            // Xử lý các lỗi hệ thống khác phát sinh trong quá trình lọc
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return Arrays.stream(Constants.WHITE_LIST_URL)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}