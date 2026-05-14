package com.manager.studio.managerstudio.service.implement;

import com.manager.studio.managerstudio.entity.RoleEntity;
import com.manager.studio.managerstudio.repository.RoleRepository;
import com.manager.studio.managerstudio.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {

    private  final RoleRepository roleRepository;
    @Override
    public RoleEntity save(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }
}
