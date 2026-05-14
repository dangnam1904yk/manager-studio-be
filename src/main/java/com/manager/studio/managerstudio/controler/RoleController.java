package com.manager.studio.managerstudio.controler;

import com.manager.studio.managerstudio.entity.RoleEntity;
import com.manager.studio.managerstudio.service.RoleService;
import com.manager.studio.managerstudio.util.ApiResponse;
import com.manager.studio.managerstudio.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.PREFIX_API_PRIVATE +"/role")
@RequiredArgsConstructor
public class RoleController {

    private  final RoleService roleService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<RoleEntity>> saveRole(@RequestBody @Validated RoleEntity roleEntity){
        return ResponseEntity.ok(ApiResponse.success(roleService.save(roleEntity)));
    }
 }
