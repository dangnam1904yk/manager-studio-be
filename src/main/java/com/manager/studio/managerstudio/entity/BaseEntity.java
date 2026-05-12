package com.manager.studio.managerstudio.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class BaseEntity {
    LocalDateTime createDate;
    LocalDateTime lastModifyDate;
    String createBy;
    String lastModifyBy;
}
