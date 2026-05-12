package com.manager.studio.managerstudio.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleEntity extends  BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String code;

    String name;
}
