package com.manager.studio.managerstudio.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImgChoseRequest {
    private String urlDriver;
    private List<ImgChose> imgChoses;
}
