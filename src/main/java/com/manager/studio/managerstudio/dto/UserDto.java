package com.manager.studio.managerstudio.dto;

import com.manager.studio.managerstudio.dto.base.BaseDto;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto extends BaseDto {

    String username;

    String password;

    String phoneNumber;

    String studioName;

    String level;

    boolean isManager;
}
