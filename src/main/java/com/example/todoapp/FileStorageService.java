package com.example.todoapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path storageDirectory;

    public FileStorageService(@Value("${app.file-storage.directory:uploads}") String directory) {
        try {
            this.storageDirectory = Paths.get(directory).toAbsolutePath().normalize();
            Files.createDirectories(this.storageDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("ファイル保存先を作成できません", exception);
        }
    }

    public StoredFile store(MultipartFile file) {
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + (extension == null ? "" : "." + extension);
        Path target = resolveStoredPath(storedFilename);
        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException exception) {
            throw new IllegalStateException("ファイルを保存できません", exception);
        }
        return new StoredFile(storedFilename, originalFilename, file.getContentType(), file.getSize());
    }

    public Path load(String storedFilename) {
        return resolveStoredPath(storedFilename);
    }

    public void delete(String storedFilename) {
        try {
            Files.deleteIfExists(resolveStoredPath(storedFilename));
        } catch (IOException exception) {
            throw new IllegalStateException("ファイルを削除できません", exception);
        }
    }

    private String sanitizeFilename(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        if (cleaned.isBlank() || ".".equals(cleaned) || "..".equals(cleaned)
                || cleaned.contains("..")) {
            throw new IllegalArgumentException("安全でないファイル名です");
        }
        String filename = Paths.get(cleaned).getFileName().toString();
        if (filename.isBlank()) {
            throw new IllegalArgumentException("ファイル名がありません");
        }
        return filename;
    }

    private Path resolveStoredPath(String storedFilename) {
        Path resolved = storageDirectory.resolve(storedFilename).normalize();
        if (!resolved.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("安全でない保存先です");
        }
        return resolved;
    }

    public record StoredFile(String storedFilename, String originalFilename,
            String contentType, long size) {
    }
}
