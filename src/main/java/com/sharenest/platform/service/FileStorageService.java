package com.sharenest.platform.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not create uploads directory.", exception);
        }
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF and WebP images are allowed.");
        }

        String storedFilename = UUID.randomUUID() + "." + extension.toLowerCase(Locale.ROOT);
        Path destination = uploadPath.resolve(storedFilename).normalize();
        if (!destination.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid image file name.");
        }

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not store uploaded image.", exception);
        }

        return "/uploads/" + storedFilename;
    }
}
