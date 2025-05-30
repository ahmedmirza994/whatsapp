/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.ah.whatsapp.enums.FolderName;

public interface FileStorage {

	/**
	 * Stores a file in a specified category.
	 *
	 * @param file The multipart file to store.
	 * @param folderName The folder (subdirectory) for the file.
	 * @param baseFilename The desired base name for the file (without extension).
	 * @return The generated filename (including extension) of the stored file.
	 * @throws IOException If an error occurs during file storage.
	 */
	String storeFile(MultipartFile file, FolderName folderName, String baseFilename)
			throws IOException;

	/**
	 * Loads a file as a resource.
	 *
	 * @param folderName The folder (subdirectory) of the file.
	 * @param filename The name of the file to load.
	 * @return The file as a Resource.
	 * @throws MalformedURLException If the path is invalid.
	 */
	Resource loadFileAsResource(FolderName folderName, String filename)
			throws MalformedURLException;
}
