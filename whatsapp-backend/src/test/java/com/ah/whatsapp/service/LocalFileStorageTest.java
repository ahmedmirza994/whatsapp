/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.service.impl.LocalFileStorage;

/**
 * Unit tests for LocalFileStorage implementation.
 * Tests the concrete implementation behavior, not the interface.
 */
class LocalFileStorageTest {

	@TempDir Path tempDir;

	private LocalFileStorage fileStorage;
	private MockMultipartFile mockFile;

	@BeforeEach
	void setUp() {
		fileStorage = new LocalFileStorage(tempDir.toString());

		mockFile =
				new MockMultipartFile(
						"file", "test-image.jpg", "image/jpeg", "test content".getBytes());
	}

	@Test
	void storeFile_WithValidInputs_ShouldStoreFileSuccessfully() throws IOException {
		// Given
		String baseFilename = "profile-pic";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String storedFilename = fileStorage.storeFile(mockFile, folderName, baseFilename);

		// Then
		assertNotNull(storedFilename);
		assertTrue(storedFilename.contains(baseFilename));
		assertTrue(storedFilename.endsWith(".jpg"));

		// Verify file exists in correct folder
		Path expectedPath = tempDir.resolve("profile_pictures").resolve(storedFilename);
		assertTrue(Files.exists(expectedPath));

		// Verify file content
		byte[] storedContent = Files.readAllBytes(expectedPath);
		assertArrayEquals("test content".getBytes(), storedContent);
	}

	@Test
	void storeFile_WithNullFile_ShouldThrowIllegalArgumentException() {
		// Given
		MultipartFile nullFile = null;
		String baseFilename = "test";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then - Null file should throw IllegalArgumentException
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.storeFile(nullFile, folderName, baseFilename));

		assertEquals("File cannot be null", exception.getMessage());
	}

	@Test
	void storeFile_WithEmptyFile_ShouldThrowIllegalArgumentException() {
		// Given
		MockMultipartFile emptyFile =
				new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
		String baseFilename = "test";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.storeFile(emptyFile, folderName, baseFilename));

		assertEquals("Cannot store empty file", exception.getMessage());
	}

	@Test
	void storeFile_WithNullFolderName_ShouldThrowIllegalArgumentException() {
		// Given
		FolderName nullFolder = null;
		String baseFilename = "test";

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.storeFile(mockFile, nullFolder, baseFilename));

		assertEquals("Folder name cannot be null", exception.getMessage());
	}

	@Test
	void storeFile_WithNullBaseFilename_ShouldThrowIllegalArgumentException() {
		// Given
		String nullBaseFilename = null;
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.storeFile(mockFile, folderName, nullBaseFilename));

		assertEquals("Base filename cannot be null or empty", exception.getMessage());
	}

	@Test
	void storeFile_WithEmptyBaseFilename_ShouldThrowIllegalArgumentException() {
		// Given
		String emptyBaseFilename = "";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.storeFile(mockFile, folderName, emptyBaseFilename));

		assertEquals("Base filename cannot be null or empty", exception.getMessage());
	}

	@Test
	void storeFile_WithWhitespaceOnlyBaseFilename_ShouldThrowIllegalArgumentException() {
		// Given
		String whitespaceFilename = "   ";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.storeFile(mockFile, folderName, whitespaceFilename));

		assertEquals("Base filename cannot be null or empty", exception.getMessage());
	}

	@Test
	void storeFile_WithSpecialCharactersInBaseFilename_ShouldSanitizeFilename() throws IOException {
		// Given
		String baseFilename = "user@#$%^&*()name";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String storedFilename = fileStorage.storeFile(mockFile, folderName, baseFilename);

		// Then
		assertNotNull(storedFilename);
		// Should not contain special characters
		assertFalse(storedFilename.contains("@"));
		assertFalse(storedFilename.contains("#"));
		assertFalse(storedFilename.contains("$"));
		assertFalse(storedFilename.contains("%"));
		assertFalse(storedFilename.contains("^"));
		assertFalse(storedFilename.contains("&"));
		assertFalse(storedFilename.contains("*"));
		assertFalse(storedFilename.contains("("));
		assertFalse(storedFilename.contains(")"));
	}

	@Test
	void storeFile_WithDifferentFileTypes_ShouldPreserveExtension() throws IOException {
		// Given
		MockMultipartFile pngFile =
				new MockMultipartFile("file", "test.png", "image/png", "png content".getBytes());
		MockMultipartFile pdfFile =
				new MockMultipartFile(
						"file", "document.pdf", "application/pdf", "pdf content".getBytes());
		String baseFilename = "test-file";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String pngFilename = fileStorage.storeFile(pngFile, folderName, baseFilename);
		String pdfFilename = fileStorage.storeFile(pdfFile, folderName, baseFilename);

		// Then
		assertTrue(pngFilename.endsWith(".png"));
		assertTrue(pdfFilename.endsWith(".pdf"));
	}

	@Test
	void storeFile_WithFileWithoutExtension_ShouldHandleGracefully() throws IOException {
		// Given
		MockMultipartFile fileWithoutExt =
				new MockMultipartFile("file", "filename", "text/plain", "content".getBytes());
		String baseFilename = "test-file";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String storedFilename = fileStorage.storeFile(fileWithoutExt, folderName, baseFilename);

		// Then
		assertNotNull(storedFilename);
		assertTrue(storedFilename.contains(baseFilename));
		// Should not have extension since original didn't have one
	}

	@Test
	void storeFile_WithVeryLongBaseFilename_ShouldTruncateAppropriately() throws IOException {
		// Given
		String longBaseFilename = "a".repeat(300); // Very long filename
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When
		String storedFilename = fileStorage.storeFile(mockFile, folderName, longBaseFilename);

		// Then
		assertNotNull(storedFilename);
		// Most filesystems limit to 255 characters
		assertTrue(storedFilename.length() <= 255);
	}

	@Test
	void storeFile_WithDuplicateBasename_ShouldReplaceExistingFile() throws IOException {
		// Given
		String baseFilename = "profile-pic";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		MockMultipartFile firstFile =
				new MockMultipartFile(
						"file", "test-image.jpg", "image/jpeg", "first content".getBytes());
		MockMultipartFile secondFile =
				new MockMultipartFile(
						"file", "test-image.jpg", "image/jpeg", "second content".getBytes());

		// When - Store multiple files with same base name
		String firstFilename = fileStorage.storeFile(firstFile, folderName, baseFilename);
		String secondFilename = fileStorage.storeFile(secondFile, folderName, baseFilename);

		// Then - Filenames should be the same (replacement behavior)
		assertEquals(firstFilename, secondFilename);
		assertTrue(firstFilename.contains(baseFilename));
		assertTrue(firstFilename.endsWith(".jpg"));

		// Verify only one file exists and it contains the latest content
		Path filePath = tempDir.resolve("profile_pictures").resolve(firstFilename);
		assertTrue(Files.exists(filePath));

		byte[] storedContent = Files.readAllBytes(filePath);
		assertArrayEquals("second content".getBytes(), storedContent);
	}

	@Test
	void storeFile_ShouldCreateFolderIfNotExists() throws IOException {
		// Given
		String baseFilename = "test-file";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// Ensure folder doesn't exist initially
		Path folderPath = tempDir.resolve("profile_pictures");
		assertFalse(Files.exists(folderPath));

		// When
		String storedFilename = fileStorage.storeFile(mockFile, folderName, baseFilename);

		// Then
		assertNotNull(storedFilename);
		assertTrue(Files.exists(folderPath));
		assertTrue(Files.isDirectory(folderPath));
	}

	@Test
	void loadFileAsResource_WithValidFile_ShouldReturnResource() throws IOException {
		// Given
		String baseFilename = "test-file";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// First store a file
		String storedFilename = fileStorage.storeFile(mockFile, folderName, baseFilename);

		// When
		Resource resource = fileStorage.loadFileAsResource(folderName, storedFilename);

		// Then
		assertNotNull(resource);
		assertTrue(resource.exists());
		assertTrue(resource.isReadable());
		assertEquals(mockFile.getSize(), resource.contentLength());
	}

	@Test
	void loadFileAsResource_WithNonExistentFile_ShouldThrowRuntimeException() {
		// Given
		String nonExistentFilename = "non-existent-file.jpg";
		FolderName folderName = FolderName.PROFILE_PICTURES;

		// When & Then
		RuntimeException exception =
				assertThrows(
						RuntimeException.class,
						() -> fileStorage.loadFileAsResource(folderName, nonExistentFilename));

		assertTrue(exception.getMessage().contains("Could not read file"));
		assertTrue(exception.getMessage().contains(nonExistentFilename));
	}

	@Test
	void loadFileAsResource_WithNullFolderName_ShouldThrowIllegalArgumentException() {
		// Given
		FolderName nullFolder = null;
		String filename = "test.jpg";

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.loadFileAsResource(nullFolder, filename));

		assertEquals("Folder name cannot be null", exception.getMessage());
	}

	@Test
	void loadFileAsResource_WithNullFilename_ShouldThrowIllegalArgumentException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;
		String nullFilename = null;

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.loadFileAsResource(folderName, nullFilename));

		assertEquals("Filename cannot be null or empty", exception.getMessage());
	}

	@Test
	void loadFileAsResource_WithEmptyFilename_ShouldThrowIllegalArgumentException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;
		String emptyFilename = "";

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.loadFileAsResource(folderName, emptyFilename));

		assertEquals("Filename cannot be null or empty", exception.getMessage());
	}

	@Test
	void loadFileAsResource_WithWhitespaceOnlyFilename_ShouldThrowIllegalArgumentException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;
		String whitespaceFilename = "   ";

		// When & Then
		IllegalArgumentException exception =
				assertThrows(
						IllegalArgumentException.class,
						() -> fileStorage.loadFileAsResource(folderName, whitespaceFilename));

		assertEquals("Filename cannot be null or empty", exception.getMessage());
	}

	@Test
	void loadFileAsResource_WithInvalidPath_ShouldThrowRuntimeException() {
		// Given
		FolderName folderName = FolderName.PROFILE_PICTURES;
		String invalidFilename = "../../../etc/passwd"; // Path traversal attempt

		// When & Then
		RuntimeException exception =
				assertThrows(
						RuntimeException.class,
						() -> fileStorage.loadFileAsResource(folderName, invalidFilename));

		assertTrue(exception.getMessage().contains("Could not read file"));
	}

	@Test
	void storeFile_WithIOException_ShouldPropagateException() throws IOException {
		// Given - Use a read-only directory to force IOException
		Path readOnlyDir = tempDir.resolve("readonly");
		Files.createDirectories(readOnlyDir);
		readOnlyDir.toFile().setReadOnly();

		LocalFileStorage readOnlyStorage = new LocalFileStorage(readOnlyDir.toString());

		// When & Then
		assertThrows(
				IOException.class,
				() -> {
					readOnlyStorage.storeFile(mockFile, FolderName.PROFILE_PICTURES, "test");
				});
	}

	@Test
	void constructor_WithNullRootPath_ShouldThrowIllegalArgumentException() {
		// When & Then
		IllegalArgumentException exception =
				assertThrows(IllegalArgumentException.class, () -> new LocalFileStorage(null));

		assertEquals("Root path cannot be null or empty", exception.getMessage());
	}

	@Test
	void constructor_WithEmptyRootPath_ShouldThrowIllegalArgumentException() {
		// When & Then
		IllegalArgumentException exception =
				assertThrows(IllegalArgumentException.class, () -> new LocalFileStorage(""));

		assertEquals("Root path cannot be null or empty", exception.getMessage());
	}

	@Test
	void constructor_WithWhitespaceOnlyRootPath_ShouldThrowIllegalArgumentException() {
		// When & Then
		IllegalArgumentException exception =
				assertThrows(IllegalArgumentException.class, () -> new LocalFileStorage("   "));

		assertEquals("Root path cannot be null or empty", exception.getMessage());
	}
}
