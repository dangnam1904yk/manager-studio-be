package com.manager.studio.managerstudio.service;

import com.manager.studio.managerstudio.dto.Profile;
import com.manager.studio.managerstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public Profile getMyInfo() {
        // Lấy username (email) từ SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.isNull(authentication)) throw  new RuntimeException();
        String username = authentication.getName();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Profile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()))
                .build();
    }
}