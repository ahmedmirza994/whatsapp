package com.ah.whatsapp.service.impl;

import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.service.FileStorage;
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

@Service
public class LocalFileStorage implements FileStorage {

	private final Path baseStoragePath;


	public LocalFileStorage(@Value("${app.storage.base-path}") String storageBasePath) {
		String homeFolder = System.getProperty("user.home");
		if (storageBasePath != null) {
			storageBasePath = homeFolder + File.separator + storageBasePath;
		}
		try {
			assert storageBasePath != null;
			this.baseStoragePath = Paths.get(storageBasePath).toAbsolutePath().normalize();
			Files.createDirectories(baseStoragePath);
		} catch (Exception e) {
			throw new RuntimeException("Could not create base storage directory!" + storageBasePath, e);
		}
	}


	@Override
	public String storeFile(MultipartFile file, FolderName folderName, String baseFilename) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File cannot be empty.");
		}
		if (folderName == null) {
			throw new IllegalArgumentException("Folder Name cannot be empty.");
		}
		if (baseFilename == null || baseFilename.isBlank()) {
			throw new IllegalArgumentException("Base filename cannot be empty.");
		}

		String normalizedCategory = StringUtils.cleanPath(folderName.name());
		String normalizedBaseFilename = StringUtils.cleanPath(baseFilename);

		Path folderPath = this.baseStoragePath.resolve(normalizedCategory);
		try {
			Files.createDirectories(folderPath);
		} catch (IOException ex) {
			throw new RuntimeException("Could not create category storage directory: " + normalizedCategory, ex);
		}

		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
		String fileExtension = "";
		if (originalFilename.contains(".")) {
			fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		if (normalizedBaseFilename.contains("/") || normalizedBaseFilename.contains("\\") || normalizedBaseFilename.equals("..")) {
			throw new IllegalArgumentException("Invalid base filename: " + baseFilename);
		}

		String newFilenameWithExtension = normalizedBaseFilename + fileExtension;
		Path targetLocation = folderPath.resolve(newFilenameWithExtension);
		Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		return newFilenameWithExtension;
	}

	@Override
	public Resource loadFileAsResource(FolderName folderName, String filename) throws MalformedURLException {
		if (folderName == null || filename == null || filename.isBlank()) {
			throw new IllegalArgumentException("Category and filename cannot be empty.");
		}
		String normalizedCategory = StringUtils.cleanPath(folderName.name());
		String normalizedFilename = StringUtils.cleanPath(filename);

		if (normalizedFilename.contains("/") || normalizedFilename.contains("\\") || normalizedFilename.equals("..")) {
			throw new IllegalArgumentException("Invalid filename: " + filename);
		}

		Path filePath = this.baseStoragePath.resolve(normalizedCategory).resolve(normalizedFilename).normalize();
		Resource resource = new UrlResource(filePath.toUri());
		if (resource.exists() && resource.isReadable()) {
			return resource;
		} else {
			throw new RuntimeException("Could not read file: " + filename + " in folder: " + folderName.name());
		}
	}
}
