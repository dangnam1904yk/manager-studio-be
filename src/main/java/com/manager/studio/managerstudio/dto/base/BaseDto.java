package com.manager.studio.managerstudio.dto.base;

import com.manager.studio.managerstudio.enums.Status;
import com.manager.studio.managerstudio.util.UUIDv7;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseDto {

    UUID id;

    LocalDateTime createDate;

    LocalDateTime lastModifyDate;

    String createBy;

    String lastModifyBy;

    Status status;
}
