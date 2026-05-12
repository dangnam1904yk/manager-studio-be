package com.manager.studio.managerstudio.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.manager.studio.managerstudio.request.ImgChoseRequest;
import com.manager.studio.managerstudio.request.UrlDriver;
import com.manager.studio.managerstudio.request.Img;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final Drive driveService;

    // Giới hạn 5 request đồng thời để tránh bị Google chặn (Connection Reset)
    private final Semaphore semaphore = new Semaphore(5);

    // Sử dụng một instance RestTemplate duy nhất để tối ưu Connection Pool
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Trích xuất Folder ID từ link Google Drive
     */
    private String extractFolderId(String url) {
        if (url == null || url.isEmpty()) return "";
        String regex = "/folders/([a-zA-Z0-9_-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    /**
     * STREAMING API: Lấy ảnh đến đâu đẩy về FE đến đó (Dùng Java 21 Virtual Threads)
     */
    public void listImagesVirtualThread(UrlDriver urlDriver, Consumer<Img> callback) throws IOException {
        String folderId = extractFolderId(urlDriver.getUrl());

        // 1. Lấy danh sách file metadata từ Google Drive
        var result = driveService.files().list()
                .setQ("'" + folderId + "' in parents and trashed = false")
                .setFields("files(id, name, thumbnailLink)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) return;

        // 2. Sử dụng Virtual Thread Executor để xử lý song song từng file
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (File file : files) {
                executor.submit(() -> {
                    try {
                        // Đợi đến lượt để không làm quá tải băng thông
                        semaphore.acquire();

                        Img img = new Img();
                        img.setId(file.getId());
                        img.setName(file.getName());

                        String thumb = file.getThumbnailLink();
                        if (thumb != null) {
                            thumb = thumb.replace("=s220", "=s400"); // Tăng chất lượng thumb
                        }
                        img.setThumbnail(thumb);
                        img.setOriginal("https://drive.google.com/uc?export=view&id=" + file.getId());

                        // 3. Tải bytes với cơ chế Retry khi gặp lỗi mạng
//                        byte[] imageBytes = getResouresImgWithRetry(file.getId());
//                        img.setBytes(imageBytes);

                        // 4. Gọi callback để đẩy dữ liệu qua SseEmitter về FE
                        callback.accept(img);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Virtual Thread bị gián đoạn: {}", e.getMessage());
                    } finally {
                        // Giải phóng vị trí cho ảnh tiếp theo
                        semaphore.release();
                    }
                });
            }
        } // Executor tự động đóng sau khi tất cả Virtual Threads hoàn thành
    }

    /**
     * Tải Byte ảnh từ Google Drive với cơ chế thử lại (Retry)
     */
    public byte[] getResouresImgWithRetry(String imgId) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                String url = "https://drive.google.com/uc?export=download&id=" + imgId;
                return restTemplate.getForObject(url, byte[].class);
            } catch (Exception e) {
                log.warn("Lần thử {} thất bại cho file {}: {}", (i + 1), imgId, e.getMessage());
                if (i == maxRetries - 1) {
                    log.error("Bỏ qua ảnh {} sau 3 lần thử thất bại.", imgId);
                    return null;
                }
                try {
                    // Nghỉ tăng dần: 1s, 2s... trước khi thử lại
                    Thread.sleep(1000L * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    /**
     * Tạo Shortcuts cho khách hàng chọn ảnh (Batch Request)
     */
    public void createShortcuts(String customerFolderName, List<String> fileIds) throws IOException {
        File folderMetadata = new File()
                .setName(customerFolderName)
                .setMimeType("application/vnd.google-apps.folder");

        File customerFolder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();

        String customerFolderId = customerFolder.getId();
        com.google.api.client.googleapis.batch.BatchRequest batch = driveService.batch();

        for (String fileId : fileIds) {
            File shortcutMetadata = new File()
                    .setName("Selected_" + fileId)
                    .setMimeType("application/vnd.google-apps.shortcut")
                    .setShortcutDetails(new File.ShortcutDetails().setTargetId(fileId))
                    .setParents(Collections.singletonList(customerFolderId));

            driveService.files().create(shortcutMetadata).queue(batch, new com.google.api.client.googleapis.batch.json.JsonBatchCallback<File>() {
                @Override
                public void onSuccess(File file, com.google.api.client.http.HttpHeaders responseHeaders) {}
                @Override
                public void onFailure(com.google.api.client.googleapis.json.GoogleJsonError e, com.google.api.client.http.HttpHeaders responseHeaders) {
                    log.error("Lỗi tạo shortcut: {}", e.getMessage());
                }
            });
        }
        batch.execute();
    }

    /**
     * Các hàm lấy dữ liệu truyền thống (không stream)
     */
    public List<Img> listImagesA(UrlDriver urlDriver) throws IOException {
        String folderId = extractFolderId(urlDriver.getUrl());
        var result = driveService.files().list()
                .setQ("'" + folderId + "' in parents and trashed = false")
                .setFields("files(id, name, thumbnailLink)")
                .setPageSize(20) // Giới hạn để an toàn RAM
                .execute();

        if (result.getFiles() == null) return new ArrayList<>();

        // Sử dụng Virtual Threads xử lý nhanh cho List nhỏ
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Img>> futures = result.getFiles().stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> {
                        Img img = new Img();
                        img.setId(file.getId());
                        img.setName(file.getName());
                        String thumb = file.getThumbnailLink();
                        if (thumb != null) thumb = thumb.replace("=s220", "=s400");
                        img.setThumbnail(thumb);
                        img.setBytes(getResouresImgWithRetry(file.getId()));
                        return img;
                    }, executor))
                    .toList();

            return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        }
    }

    /**
     * Lấy ảnh theo trang (Phân trang) và trả về Token của trang kế tiếp
     */
    public String listImagesWithPage(UrlDriver urlDriver, String pageToken, Consumer<Img> callback) throws IOException {
        String folderId = extractFolderId(urlDriver.getUrl());

        // 1. Gọi API Google Drive với PageToken
        var result = driveService.files().list()
                .setQ("'" + folderId + "' in parents and trashed = false")
                .setFields("nextPageToken, files(id, name, thumbnailLink)") // Lấy thêm nextPageToken
                .setPageSize(30) // Mỗi lần cuộn sẽ lấy thêm 30 ảnh
                .setPageToken(pageToken) // Truyền token nhận được từ FE vào đây
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) return null;

        // 2. Sử dụng Virtual Thread để xử lý song song các ảnh trong trang này
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (File file : files) {
                executor.submit(() -> {
                    try {
                        semaphore.acquire();

                        Img img = new Img();
                        img.setId(file.getId());
                        img.setName(file.getName());

                        String thumb = file.getThumbnailLink();
                        if (thumb != null) thumb = thumb.replace("=s220", "=s400");
                        img.setThumbnail(thumb);

                        // Chỉ gửi Metadata (không gửi bytes để tránh Out of Memory)
                        callback.accept(img);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }
                });
            }
        }

        // 3. Trả về token của trang sau (nếu hết ảnh nó sẽ trả về null)
        return result.getNextPageToken();
    }

    public SseEmitter streamImage(UrlDriver urlDriver ){
        // Timeout 5 phút
        SseEmitter emitter = new SseEmitter(300000L);

        Thread.ofVirtual().start(() -> {
            try {
                // Giả sử hàm trả về nextPageToken sau khi đã duyệt xong danh sách ảnh hiện tại
                String nextPageToken = listImagesWithPage(urlDriver, urlDriver.getPageToken(), img -> {
                    try {
                        emitter.send(SseEmitter.event().name("image-record").data(img));
                    } catch (IOException e) {
                        log.warn(e.getMessage());
                        // FE có thể đã đóng kết nối
                    }
                });

                // Gửi token của trang tiếp theo về cho Frontend
                if (nextPageToken != null && !nextPageToken.isEmpty()) {
                    emitter.send(SseEmitter.event().name("next-page").data(nextPageToken));
                }

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return  emitter;
    }

    public ImgChoseRequest chooseImg( ImgChoseRequest imgChoseRequest){

        return  imgChoseRequest;
    }
}