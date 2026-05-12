package com.manager.studio.managerstudio.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class DriverBean {

    @Bean
    public Drive googleDriveService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Đọc file JSON từ folder src/main/resources
        ClassPathResource resource = new ClassPathResource("static/client_secret.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream())
                .createScoped(Collections.singleton(DriveScopes.DRIVE_READONLY));

        return new Drive.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("ManagerStudio")
                .build();
    }
}