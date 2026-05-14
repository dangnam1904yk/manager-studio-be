package com.manager.studio.managerstudio.entity;

import com.manager.studio.managerstudio.entity.base.BaseEntity;
import com.manager.studio.managerstudio.util.UUIDv7;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_role",indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_role_id", columnList = "role_id"),
})
@Entity
@Builder
public class UserRoleEntity {

    @Id
    @UUIDv7
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;
}
