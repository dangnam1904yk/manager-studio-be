package com.manager.studio.managerstudio.controler;

import com.manager.studio.managerstudio.request.ImgChoseRequest;
import com.manager.studio.managerstudio.service.DriverService;
import com.manager.studio.managerstudio.request.UrlDriver;
import com.manager.studio.managerstudio.request.Img;
import com.manager.studio.managerstudio.util.ApiResponse;
import com.manager.studio.managerstudio.util.Constants;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.PREFIX_API_PUBLIC+"/driver")
public class DriverController {

    private  final DriverService driverService;

    @PostMapping("getImg")
    public ResponseEntity<ApiResponse<List<Img>>>  getImg(@RequestBody UrlDriver driver) throws IOException {
    return  ResponseEntity.ok(ApiResponse.success(driverService.listImagesA(driver)));
    }

    @PostMapping(value = "/stream-images", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamImages(@RequestBody UrlDriver urlDriver) {
        return  driverService.streamImage(urlDriver);
    }

    @PostMapping(value = "/choose-img")
    public ResponseEntity<ApiResponse<ImgChoseRequest>> streamImages(@RequestBody  @Validated ImgChoseRequest urlDriver) {
        return ResponseEntity.ok(ApiResponse.success(driverService.chooseImg(urlDriver)));
    }

    @GetMapping("/proxy-image/{fileId}")
    public ResponseEntity<byte[]> proxyImage(@PathVariable String fileId) {
        try {

            byte[] imageBytes = driverService.getResouresImgWithRetry(fileId);

            if (imageBytes == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            // Cho phép trình duyệt cache ảnh trong 1 ngày (86400 giây)
            headers.setCacheControl("max-age=86400");

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
