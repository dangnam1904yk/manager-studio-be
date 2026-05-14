package com.manager.studio.managerstudio.enums;

import lombok.Getter;

@Getter
public enum Status {
    APPROVE("APPROVE"), DRAFT("DRAFT"), ACTIVE("ACTIVE"), NO_ACTIVE("NO_ACTIVE");
    private  final  String value;

    Status(String value){ this.value =value;}
}
