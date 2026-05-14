package com.manager.studio.managerstudio.entity;

import com.manager.studio.managerstudio.entity.base.BaseEntity;
import com.manager.studio.managerstudio.util.UUIDv7;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "permission", indexes = {
        @Index(name = "idx_code", columnList = "code"),
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionEntity extends BaseEntity {

    String code;

    String name;

    String url;
}
