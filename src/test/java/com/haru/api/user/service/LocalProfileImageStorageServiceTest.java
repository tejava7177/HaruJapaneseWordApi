package com.haru.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.haru.api.config.ProfileImageStorageProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class LocalProfileImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeProfileImage_savesFileAndReturnsUrl() throws Exception {
        LocalProfileImageStorageService storageService = new LocalProfileImageStorageService(
                new ProfileImageStorageProperties(tempDir.toString(), "/uploads/profile")
        );
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());

        String storedUrl = storageService.storeProfileImage(2L, file);

        assertThat(storedUrl).startsWith("/uploads/profile/2-").endsWith(".png");
        String storedFileName = storedUrl.substring("/uploads/profile/".length());
        assertThat(Files.readString(tempDir.resolve(storedFileName))).isEqualTo("image-bytes");
    }

    @Test
    void storeProfileImage_rejectsNonImageFile() {
        LocalProfileImageStorageService storageService = new LocalProfileImageStorageService(
                new ProfileImageStorageProperties(tempDir.toString(), "/uploads/profile")
        );
        MockMultipartFile file = new MockMultipartFile("file", "profile.txt", "text/plain", "not-image".getBytes());

        assertThatThrownBy(() -> storageService.storeProfileImage(2L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Profile image must be an image file");
    }

    @Test
    void storeProfileImage_rejectsEmptyFile() {
        LocalProfileImageStorageService storageService = new LocalProfileImageStorageService(
                new ProfileImageStorageProperties(tempDir.toString(), "/uploads/profile")
        );
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> storageService.storeProfileImage(2L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Profile image file is required");
    }
}
