package com.manager.studio.managerstudio.entity;

import com.manager.studio.managerstudio.entity.base.BaseEntity;
import com.manager.studio.managerstudio.util.UUIDv7;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user",indexes = {
        @Index(name = "idx_username", columnList = "username"),
},uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username")
})
@Entity
@Builder
public class UserEntity extends BaseEntity implements UserDetails {

    String username;

    String password;

    String phoneNumber;

    String studioName;

    String fullName;

    String level;

    boolean isManager;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserRoleEntity> userRoles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getCode()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
