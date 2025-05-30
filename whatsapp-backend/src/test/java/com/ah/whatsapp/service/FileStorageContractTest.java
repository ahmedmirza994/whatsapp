/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import com.ah.whatsapp.enums.FolderName;

/**
 * Contract tests for FileStorage implementations.
 * These tests define the expected behavior that ALL FileStorage implementations must follow.
 *
 * This is an advanced testing pattern - extend this class for each implementation
 * to ensure they all adhere to the same contract.
 *
 * Usage:
 * 1. Create concrete test class extending this (e.g., LocalFileStorageContractTest)
 * 2. Implement createFileStorage() method
 * 3. All contract tests will run for your implementation
 */
public abstract class FileStorageContractTest {

	protected FileStorage fileStorage;
	protected MockMultipartFile validFile;

	/**
	 * Subclasses must implement this to provide their specific FileStorage implementation.
	 * @return A configured FileStorage instance for testing
	 */
	protected abstract FileStorage createFileStorage();

	@BeforeEach
	void setUp() {
		fileStorage = createFileStorage();
		validFile =
				new MockMultipartFile(
						"file",
						"test-image.jpg",
						"image/jpeg",
						"test content for contract testing".getBytes());
	}

	// Contract: Basic file storage must work
	@Test
	void contract_storeFile_WithValidInputs_MustStoreSuccessfully() throws IOException {
		// Given
		String baseFilename = "contract-test";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String storedFilename = fileStorage.storeFile(validFile, folderName, baseFilename);

		// Then - Contract requirements
		assertNotNull(storedFilename, "Stored filename must not be null");
		assertFalse(storedFilename.trim().isEmpty(), "Stored filename must not be empty");
		assertTrue(
				storedFilename.contains(baseFilename),
				"Stored filename must contain base filename");
	}

	// Contract: Must handle null file parameter
	@Test
	void contract_storeFile_WithNullFile_MustThrowException() {
		// Given
		String baseFilename = "test";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.storeFile(null, folderName, baseFilename),
				"Must throw IllegalArgumentException for null file");
	}

	// Contract: Must handle empty file
	@Test
	void contract_storeFile_WithEmptyFile_MustThrowException() {
		// Given
		MockMultipartFile emptyFile =
				new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
		String baseFilename = "test";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.storeFile(emptyFile, folderName, baseFilename),
				"Must throw IllegalArgumentException for empty file");
	}

	// Contract: Must handle null folder name
	@Test
	void contract_storeFile_WithNullFolderName_MustThrowException() {
		// Given
		String baseFilename = "test";

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.storeFile(validFile, null, baseFilename),
				"Must throw IllegalArgumentException for null folder name");
	}

	// Contract: Must handle null base filename
	@Test
	void contract_storeFile_WithNullBaseFilename_MustThrowException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.storeFile(validFile, folderName, null),
				"Must throw IllegalArgumentException for null base filename");
	}

	// Contract: Must handle empty base filename
	@Test
	void contract_storeFile_WithEmptyBaseFilename_MustThrowException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.storeFile(validFile, folderName, ""),
				"Must throw IllegalArgumentException for empty base filename");
	}

	// Contract: Must preserve file extensions
	@Test
	void contract_storeFile_MustPreserveFileExtension() throws IOException {
		// Given
		MockMultipartFile pngFile =
				new MockMultipartFile("file", "test.png", "image/png", "png content".getBytes());
		String baseFilename = "test-image";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String storedFilename = fileStorage.storeFile(pngFile, folderName, baseFilename);

		// Then - Contract requirement
		assertTrue(storedFilename.endsWith(".png"), "Must preserve original file extension");
	}

	// Contract: Must generate unique filenames for duplicate base names
	@Test
	void contract_storeFile_MustGenerateUniqueFilenames() throws IOException {
		// Given
		String baseFilename = "duplicate-test";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String firstFilename = fileStorage.storeFile(validFile, folderName, baseFilename);
		String secondFilename = fileStorage.storeFile(validFile, folderName, baseFilename);

		// Then - Contract requirement
		assertNotEquals(
				firstFilename,
				secondFilename,
				"Must generate unique filenames for duplicate base names");
	}

	// Contract: Must be able to load stored files
	@Test
	void contract_loadFileAsResource_WithStoredFile_MustReturnValidResource() throws IOException {
		// Given - Store a file first
		String baseFilename = "load-test";
		FolderName folderName = FolderName.PROFILE_PICTURES;
		String storedFilename = fileStorage.storeFile(validFile, folderName, baseFilename);

		// When
		Resource resource = fileStorage.loadFileAsResource(folderName, storedFilename);

		// Then - Contract requirements
		assertNotNull(resource, "Resource must not be null");
		assertTrue(resource.exists(), "Resource must exist");
		assertTrue(resource.isReadable(), "Resource must be readable");
	}

	// Contract: Must handle null folder name in load operation
	@Test
	void contract_loadFileAsResource_WithNullFolderName_MustThrowException() {
		// Given
		String filename = "test.jpg";

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.loadFileAsResource(null, filename),
				"Must throw IllegalArgumentException for null folder name");
	}

	// Contract: Must handle null filename in load operation
	@Test
	void contract_loadFileAsResource_WithNullFilename_MustThrowException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.loadFileAsResource(folderName, null),
				"Must throw IllegalArgumentException for null filename");
	}

	// Contract: Must handle empty filename in load operation
	@Test
	void contract_loadFileAsResource_WithEmptyFilename_MustThrowException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				IllegalArgumentException.class,
				() -> fileStorage.loadFileAsResource(folderName, ""),
				"Must throw IllegalArgumentException for empty filename");
	}

	// Contract: Must handle non-existent files
	@Test
	void contract_loadFileAsResource_WithNonExistentFile_MustThrowException() {
		// Given
		String nonExistentFilename = "non-existent-file-12345.jpg";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Contract requirement
		assertThrows(
				RuntimeException.class,
				() -> fileStorage.loadFileAsResource(folderName, nonExistentFilename),
				"Must throw RuntimeException for non-existent file");
	}
}
