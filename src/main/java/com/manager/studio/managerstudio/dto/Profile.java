package com.manager.studio.managerstudio.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Profile {

    UUID id;

    String username;

    String fullName;

    String studioName;

    Set<String> roles ;
}
