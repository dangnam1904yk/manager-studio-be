package com.manager.studio.managerstudio.entity.base;

import com.manager.studio.managerstudio.enums.Status;
import com.manager.studio.managerstudio.util.UUIDv7;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Id
    @UUIDv7
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createDate;

    @LastModifiedDate
    LocalDateTime lastModifyDate;

    @CreatedBy
    String createBy;

    @LastModifiedBy
    String lastModifyBy;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20, // Nên giới hạn độ dài để tối ưu DB
            columnDefinition = "varchar(20) default 'ACTIVE'", // Thiết lập default ở tầng DB
            comment = "APPROVE: Chấp thuận, DRAFT: Nháp, ACTIVE: Hoạt động, INACTIVE: Không hoạt động"
    )
    Status status = Status.ACTIVE;
}
