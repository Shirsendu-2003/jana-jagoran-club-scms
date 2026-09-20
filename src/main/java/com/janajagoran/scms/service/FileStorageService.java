package com.janajagoran.scms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Handles file uploads for profile pictures, gallery images, event images, and notice attachments.
 * Defaults to local disk storage; swap in Cloudinary by implementing the same interface
 * (see application.properties -> app.storage.provider).
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String store(MultipartFile file, String subFolder) {
        try {
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
            String newFileName = UUID.randomUUID() + extension;

            Path targetDir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath);

            return "/uploads/" + subFolder + "/" + newFileName;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    public void delete(String relativeUrl) {
        try {
            if (relativeUrl == null || !relativeUrl.startsWith("/uploads/")) return;
            Path path = Paths.get(uploadDir, relativeUrl.substring("/uploads/".length())).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete file {}", relativeUrl, e);
        }
    }
}
