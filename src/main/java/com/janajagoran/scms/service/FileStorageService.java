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

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Backend public URL.
     * Example:
     * https://jana-jagoran-club-scms.onrender.com
     */
    @Value("${app.backend.url}")
    private String backendUrl;

    /**
     * Store uploaded file and return its public URL.
     */
    public String store(MultipartFile file, String subFolder) {
        try {
            String originalName = StringUtils.cleanPath(
                    file.getOriginalFilename() != null
                            ? file.getOriginalFilename()
                            : "file"
            );

            String extension = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";

            String newFileName = UUID.randomUUID() + extension;

            Path targetDir = Paths.get(uploadDir, subFolder)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(newFileName);

            Files.copy(file.getInputStream(), targetPath);

            // Public URL
            return backendUrl.replaceAll("/$", "")
                    + "/uploads/"
                    + subFolder
                    + "/"
                    + newFileName;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException(
                    "Failed to store file: " + e.getMessage()
            );
        }
    }

    /**
     * Delete uploaded file.
     *
     * Supports both:
     * /uploads/gallery/file.jpg
     *
     * and:
     * https://jana-jagoran-club-scms.onrender.com/uploads/gallery/file.jpg
     */
    public void delete(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return;
            }

            String relativePath;

            // Full URL
            if (fileUrl.contains("/uploads/")) {
                relativePath = fileUrl.substring(
                        fileUrl.indexOf("/uploads/") + "/uploads/".length()
                );
            } else {
                return;
            }

            Path path = Paths.get(uploadDir, relativePath)
                    .toAbsolutePath()
                    .normalize();

            Files.deleteIfExists(path);

        } catch (IOException e) {
            log.warn("Failed to delete file {}", fileUrl, e);
        }
    }
}
