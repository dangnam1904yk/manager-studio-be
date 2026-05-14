package com.manager.studio.managerstudio.controler.auth;

import com.manager.studio.managerstudio.entity.UserEntity;
import com.manager.studio.managerstudio.exception.BusinessException;
import com.manager.studio.managerstudio.request.auth.AuthenticationResponse;
import com.manager.studio.managerstudio.request.auth.LoginRequest;
import com.manager.studio.managerstudio.request.auth.RegisterRequest;
import com.manager.studio.managerstudio.service.AuthenticationService;
import com.manager.studio.managerstudio.util.ApiResponse;
import com.manager.studio.managerstudio.util.Constants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.PREFIX_API_PUBLIC+"/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserEntity>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody LoginRequest loginRequest,
                                                                     HttpServletRequest request,
                                                                     HttpServletResponse response) throws BusinessException {
        return ResponseEntity.ok(ApiResponse.success(service.login(loginRequest,request,response)));
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @CookieValue(name = "refreshToken") String refreshToken, // Lấy tự động từ Cookie
            HttpServletResponse response) {

        // Gọi service xử lý logic như cũ
        AuthenticationResponse authResponse = service.refreshToken(refreshToken);

        // Nếu bạn dùng cơ chế Rotation (Xoay vòng RT), hãy cập nhật lại Cookie mới tại đây
        Cookie newCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        newCookie.setHttpOnly(true);
        newCookie.setSecure(true);
        newCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(newCookie);

        return ResponseEntity.ok(authResponse);
    }
}
