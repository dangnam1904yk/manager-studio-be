package com.manager.studio.managerstudio.request;

import com.google.auto.value.AutoValue.Builder;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Img {
    private String name;
    private String id;
    private String thumbnail; // Link ảnh nhỏ (load nhanh)
    private String original;  // Link ảnh gốc (load khi click)
    private byte[] bytes;
}
