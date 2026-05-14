package com.manager.studio.managerstudio.dto;

import com.manager.studio.managerstudio.dto.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto  extends BaseDto {

    String code;

    String name;
}
