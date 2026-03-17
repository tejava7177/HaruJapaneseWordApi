package com.haru.api.user.service;

import com.haru.api.config.ProfileImageStorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalProfileImageStorageService implements ProfileImageStorageService {

    private final ProfileImageStorageProperties properties;

    @Override
    public String storeProfileImage(Long userId, MultipartFile file) {
        validateImage(file);

        Path storageDirectory = Path.of(properties.storagePath()).toAbsolutePath().normalize();
        String extension = resolveExtension(file);
        String filename = userId + "-" + Instant.now().toEpochMilli() + "." + extension;
        Path targetFile = storageDirectory.resolve(filename);

        try {
            Files.createDirectories(storageDirectory);
            file.transferTo(targetFile);
        } catch (IOException exception) {
            log.error("[UserProfileImage] upload failed userId={} reason={}", userId, exception.getMessage(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store profile image");
        }

        return properties.urlPrefix() + "/" + filename;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile image must be an image file");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex >= 0 && extensionIndex < filename.length() - 1) {
            return filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        }

        String contentType = file.getContentType();
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return "gif";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }

        return "img";
    }
}
