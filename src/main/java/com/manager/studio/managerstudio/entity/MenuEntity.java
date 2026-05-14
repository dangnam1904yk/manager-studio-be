package com.manager.studio.managerstudio.entity;

import com.manager.studio.managerstudio.entity.base.BaseEntity;
import com.manager.studio.managerstudio.util.UUIDv7;
import com.manager.studio.managerstudio.util.UUIDv7Generator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.IdGeneratorType;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "menu", indexes = {
        @Index(name = "idx_code", columnList = "code"),
})
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuEntity extends BaseEntity {

    String code;

    String name;

    Integer level;

    Integer orderIndex;

    String url;

    String icon;
}
