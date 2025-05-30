/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.service.FileStorage;

@Service
public class LocalFileStorage implements FileStorage {

    private final Path baseStoragePath;

    public LocalFileStorage(@Value("${app.storage.base-path}") String storageBasePath) {
        if (storageBasePath == null || storageBasePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Root path cannot be null or empty");
        }

        // If path is absolute (for testing), use it directly
        if (Paths.get(storageBasePath).isAbsolute()) {
            this.baseStoragePath = initializeStorage(storageBasePath);
        } else {
            // Otherwise, append to home directory (production)
            String homeFolder = System.getProperty("user.home");
            String finalPath = homeFolder + File.separator + storageBasePath;
            this.baseStoragePath = initializeStorage(finalPath);
        }
    }

    private Path initializeStorage(String path) {
        try {
            Path storagePath = Paths.get(path).toAbsolutePath().normalize();
            Files.createDirectories(storagePath);
            return storagePath;
        } catch (Exception e) {
            throw new RuntimeException("Could not create base storage directory!" + path, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, FolderName folderName, String baseFilename)
            throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }
        if (folderName == null) {
            throw new IllegalArgumentException("Folder name cannot be null");
        }
        if (baseFilename == null || baseFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Base filename cannot be null or empty");
        }

        String normalizedCategory = StringUtils.cleanPath(folderName.getFolderName());
        String normalizedBaseFilename = sanitizeFilename(StringUtils.cleanPath(baseFilename));

        Path folderPath = this.baseStoragePath.resolve(normalizedCategory);
        try {
            Files.createDirectories(folderPath);
        } catch (IOException ex) {
            throw ex; // Propagate IOException directly instead of wrapping in RuntimeException
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            String cleanFilename = StringUtils.cleanPath(originalFilename);
            fileExtension = cleanFilename.substring(cleanFilename.lastIndexOf("."));
        }

        if (normalizedBaseFilename.contains("/")
                || normalizedBaseFilename.contains("\\")
                || normalizedBaseFilename.equals("..")) {
            throw new IllegalArgumentException("Invalid base filename: " + baseFilename);
        }

        // Handle long filenames
        String baseWithExtension = normalizedBaseFilename + fileExtension;
        if (baseWithExtension.length() > 255) { // Most filesystems limit to 255 characters
            int maxBaseLength = 255 - fileExtension.length();
            normalizedBaseFilename =
                    normalizedBaseFilename.substring(0, Math.max(1, maxBaseLength));
        }

        // Use the base filename + extension (will replace existing file if it exists)
        String finalFilename = normalizedBaseFilename + fileExtension;
        Path targetLocation = folderPath.resolve(finalFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return finalFilename;
    }

    @Override
    public Resource loadFileAsResource(FolderName folderName, String filename)
            throws MalformedURLException {
        if (folderName == null) {
            throw new IllegalArgumentException("Folder name cannot be null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        String normalizedCategory = StringUtils.cleanPath(folderName.getFolderName());
        String normalizedFilename = StringUtils.cleanPath(filename);

        if (normalizedFilename.contains("/")
                || normalizedFilename.contains("\\")
                || normalizedFilename.equals("..")
                || normalizedFilename.startsWith("../")
                || normalizedFilename.startsWith("..\\")) {
            throw new RuntimeException("Could not read file: " + filename);
        }

        Path filePath =
                this.baseStoragePath
                        .resolve(normalizedCategory)
                        .resolve(normalizedFilename)
                        .normalize();

        // Additional security check - ensure the resolved path is still within base directory
        if (!filePath.startsWith(this.baseStoragePath)) {
            throw new RuntimeException("Could not read file: " + filename);
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException(
                    "Could not read file: "
                            + filename
                            + " in folder: "
                            + folderName.getFolderName());
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        // Remove special characters that are not allowed in filenames
        return filename.replaceAll("[^a-zA-Z0-9._-]", "");
    }
}
