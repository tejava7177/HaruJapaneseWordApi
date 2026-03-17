package com.haru.api.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorageService {

    String storeProfileImage(Long userId, MultipartFile file);
}
