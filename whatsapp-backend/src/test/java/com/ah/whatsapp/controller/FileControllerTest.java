/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.service.FileStorage;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileController Unit Tests")
class FileControllerTest {

	@Mock private FileStorage fileStorage;

	@Mock private HttpServletRequest httpServletRequest;

	@Mock private ServletContext servletContext;

	@Mock private Resource resource;

	@Mock private File file;

	@InjectMocks private FileController fileController;

	private static final String TEST_FILENAME = "test-profile-pic.jpg";
	private static final String TEST_CONTENT_TYPE = "image/jpeg";
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@Nested
	@DisplayName("getProfilePicture - Success Cases")
	class GetProfilePictureSuccessCases {

		@Test
		@DisplayName("Should return profile picture with determined content type")
		void shouldReturnProfilePictureWithDeterminedContentType() throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(resource, response.getBody());
			assertEquals(
					MediaType.parseMediaType(TEST_CONTENT_TYPE),
					response.getHeaders().getContentType());
			assertEquals(
					"public, max-age=3600",
					response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
			verify(servletContext).getMimeType("/path/to/" + TEST_FILENAME);
		}

		@Test
		@DisplayName(
				"Should return profile picture with default content type when determination fails")
		void shouldReturnProfilePictureWithDefaultContentTypeWhenDeterminationFails()
				throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME)).thenReturn(null);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(resource, response.getBody());
			assertEquals(
					MediaType.parseMediaType(DEFAULT_CONTENT_TYPE),
					response.getHeaders().getContentType());
			assertEquals(
					"public, max-age=3600",
					response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
			verify(servletContext).getMimeType("/path/to/" + TEST_FILENAME);
		}

		@Test
		@DisplayName(
				"Should return profile picture with default content type when IOException occurs"
						+ " during content type determination")
		void shouldReturnProfilePictureWithDefaultContentTypeWhenIOExceptionOccurs()
				throws Exception {
			// Given
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenThrow(new IOException("Unable to access file"));

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(resource, response.getBody());
			assertEquals(
					MediaType.parseMediaType(DEFAULT_CONTENT_TYPE),
					response.getHeaders().getContentType());
			assertEquals(
					"public, max-age=3600",
					response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}

		@Test
		@DisplayName("Should handle different image content types correctly")
		void shouldHandleDifferentImageContentTypesCorrectly() throws Exception {
			// Given
			String pngFilename = "test-profile-pic.png";
			String pngContentType = "image/png";

			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, pngFilename))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + pngFilename);
			when(servletContext.getMimeType("/path/to/" + pngFilename)).thenReturn(pngContentType);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(pngFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(resource, response.getBody());
			assertEquals(
					MediaType.parseMediaType(pngContentType),
					response.getHeaders().getContentType());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, pngFilename);
		}

		@Test
		@DisplayName("Should handle complex filename with special characters")
		void shouldHandleComplexFilenameWithSpecialCharacters() throws Exception {
			// Given
			String complexFilename = "user-123_profile.pic.2024.jpg";

			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, complexFilename))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + complexFilename);
			when(servletContext.getMimeType("/path/to/" + complexFilename))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(complexFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(resource, response.getBody());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, complexFilename);
		}
	}

	@Nested
	@DisplayName("getProfilePicture - Error Cases")
	class GetProfilePictureErrorCases {

		@Test
		@DisplayName("Should return 404 when file not found (RuntimeException)")
		void shouldReturn404WhenFileNotFound() throws Exception {
			// Given
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenThrow(new RuntimeException("File not found"));

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}

		@Test
		@DisplayName("Should return 500 when MalformedURLException occurs")
		void shouldReturn500WhenMalformedURLExceptionOccurs() throws Exception {
			// Given
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenThrow(new MalformedURLException("Invalid URL"));

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}

		@Test
		@DisplayName("Should handle null filename gracefully")
		void shouldHandleNullFilenameGracefully() throws Exception {
			// Given
			String nullFilename = null;
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, nullFilename))
					.thenThrow(new RuntimeException("Invalid filename"));

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(nullFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, nullFilename);
		}

		@Test
		@DisplayName("Should handle empty filename")
		void shouldHandleEmptyFilename() throws Exception {
			// Given
			String emptyFilename = "";
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, emptyFilename))
					.thenThrow(new RuntimeException("Invalid filename"));

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(emptyFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, emptyFilename);
		}

		@Test
		@DisplayName("Should handle filename with path traversal attempts")
		void shouldHandleFilenameWithPathTraversalAttempts() throws Exception {
			// Given
			String maliciousFilename = "../../../etc/passwd";
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, maliciousFilename))
					.thenThrow(new RuntimeException("Security violation"));

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(maliciousFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, maliciousFilename);
		}
	}

	@Nested
	@DisplayName("Edge Cases and Boundary Conditions")
	class EdgeCasesAndBoundaryConditions {

		@Test
		@DisplayName("Should handle very long filename")
		void shouldHandleVeryLongFilename() throws Exception {
			// Given
			String longFilename = "a".repeat(255) + ".jpg";
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, longFilename))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + longFilename);
			when(servletContext.getMimeType("/path/to/" + longFilename))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(longFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, longFilename);
		}

		@Test
		@DisplayName("Should handle filename with Unicode characters")
		void shouldHandleFilenameWithUnicodeCharacters() throws Exception {
			// Given
			String unicodeFilename = "用户头像_🖼️_test.jpg";
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, unicodeFilename))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + unicodeFilename);
			when(servletContext.getMimeType("/path/to/" + unicodeFilename))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(unicodeFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, unicodeFilename);
		}

		@Test
		@DisplayName("Should handle file without extension")
		void shouldHandleFileWithoutExtension() throws Exception {
			// Given
			String filenameWithoutExt = "profile-picture";
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, filenameWithoutExt))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + filenameWithoutExt);
			when(servletContext.getMimeType("/path/to/" + filenameWithoutExt)).thenReturn(null);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(filenameWithoutExt, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(
					MediaType.parseMediaType(DEFAULT_CONTENT_TYPE),
					response.getHeaders().getContentType());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, filenameWithoutExt);
		}

		@Test
		@DisplayName("Should handle multiple file extensions")
		void shouldHandleMultipleFileExtensions() throws Exception {
			// Given
			String multiExtFilename = "backup.profile.jpg.bak";
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, multiExtFilename))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + multiExtFilename);
			when(servletContext.getMimeType("/path/to/" + multiExtFilename))
					.thenReturn("application/x-backup");

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(multiExtFilename, httpServletRequest);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(
					MediaType.parseMediaType("application/x-backup"),
					response.getHeaders().getContentType());

			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, multiExtFilename);
		}
	}

	@Nested
	@DisplayName("Integration with FileStorage Service")
	class IntegrationWithFileStorageService {

		@Test
		@DisplayName("Should call fileStorage with correct parameters")
		void shouldCallFileStorageWithCorrectParameters() throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			verify(fileStorage)
					.loadFileAsResource(eq(FolderName.PROFILE_PICTURES), eq(TEST_FILENAME));
		}

		@Test
		@DisplayName("Should verify folder name is always PROFILE_PICTURES")
		void shouldVerifyFolderNameIsAlwaysProfilePictures() throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			verify(fileStorage).loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}
	}

	@Nested
	@DisplayName("HTTP Response Headers Validation")
	class HttpResponseHeadersValidation {

		@Test
		@DisplayName("Should set correct cache control headers")
		void shouldSetCorrectCacheControlHeaders() throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertEquals(
					"public, max-age=3600",
					response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
		}

		@Test
		@DisplayName("Should set correct content type header")
		void shouldSetCorrectContentTypeHeader() throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertEquals(
					MediaType.parseMediaType(TEST_CONTENT_TYPE),
					response.getHeaders().getContentType());
		}

		@Test
		@DisplayName("Should not set additional unnecessary headers")
		void shouldNotSetAdditionalUnnecessaryHeaders() throws Exception {
			// Given
			when(httpServletRequest.getServletContext()).thenReturn(servletContext);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(resource);
			when(resource.getFile()).thenReturn(file);
			when(file.getAbsolutePath()).thenReturn("/path/to/" + TEST_FILENAME);
			when(servletContext.getMimeType("/path/to/" + TEST_FILENAME))
					.thenReturn(TEST_CONTENT_TYPE);

			// When
			ResponseEntity<Resource> response =
					fileController.getProfilePicture(TEST_FILENAME, httpServletRequest);

			// Then
			assertEquals(2, response.getHeaders().size()); // Content-Type and Cache-Control only
		}
	}
}
