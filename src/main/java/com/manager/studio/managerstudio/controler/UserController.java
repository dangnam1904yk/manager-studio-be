package com.manager.studio.managerstudio.controler;

import com.manager.studio.managerstudio.dto.Profile;
import com.manager.studio.managerstudio.service.UserService;
import com.manager.studio.managerstudio.util.ApiResponse;
import com.manager.studio.managerstudio.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.PREFIX_API_PRIVATE+"/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Profile>> getMyInfo() {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo()));
    }
}
