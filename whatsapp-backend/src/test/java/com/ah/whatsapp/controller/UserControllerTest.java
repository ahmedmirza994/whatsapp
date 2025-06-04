/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.LoginDtoTestDataBuilder;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserDtoTestDataBuilder;
import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.dto.UserSignupDtoTestDataBuilder;
import com.ah.whatsapp.dto.UserUpdateDto;
import com.ah.whatsapp.dto.UserUpdateDtoTestDataBuilder;
import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.exception.UserAlreadyExistsException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.mapper.UserTestDataBuilder;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.service.FileStorage;
import com.ah.whatsapp.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

	@Mock private UserService userService;
	@Mock private UserMapper userMapper;
	@Mock private FileStorage fileStorage;
	@Mock private Resource mockResource;

	@InjectMocks private UserController userController;

	// Test data constants
	private static final UUID TEST_USER_ID = UUID.randomUUID();
	private static final UUID OTHER_USER_ID = UUID.randomUUID();
	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_PHONE = "+1234567890";
	private static final String TEST_PASSWORD = "password123";
	private static final String TEST_NAME = "Test User";
	private static final String SEARCH_QUERY = "john";
	private static final String TEST_FILENAME = "profile.jpg";

	@Nested
	@DisplayName("User Registration Tests")
	class UserRegistrationTests {

		@Test
		@DisplayName("Should successfully register a new user with valid data")
		void shouldRegisterUserSuccessfully() {
			// Given
			UserSignupDto signupDto =
					UserSignupDtoTestDataBuilder.aUserSignupDto()
							.withName(TEST_NAME)
							.withEmail(TEST_EMAIL)
							.withPassword(TEST_PASSWORD)
							.withPhone(TEST_PHONE)
							.build();

			User userModel =
					UserTestDataBuilder.aUser()
							.withName(TEST_NAME)
							.withEmail(TEST_EMAIL)
							.withPhone(TEST_PHONE)
							.build();

			UserDto expectedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withName(TEST_NAME)
							.withEmail(TEST_EMAIL)
							.withPhone(TEST_PHONE)
							.build();

			when(userMapper.toModel(signupDto)).thenReturn(userModel);
			when(userService.registerUser(userModel)).thenReturn(expectedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response = userController.signup(signupDto);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(expectedUserDto, Objects.requireNonNull(response.getBody()).getData());

			verify(userMapper, times(1)).toModel(signupDto);
			verify(userService, times(1)).registerUser(userModel);
		}

		@Test
		@DisplayName("Should handle user already exists exception during registration")
		void shouldHandleUserAlreadyExistsException() {
			// Given
			UserSignupDto signupDto =
					UserSignupDtoTestDataBuilder.aUserSignupDto().withEmail(TEST_EMAIL).build();

			User userModel = UserTestDataBuilder.aUser().withEmail(TEST_EMAIL).build();

			when(userMapper.toModel(signupDto)).thenReturn(userModel);
			when(userService.registerUser(userModel))
					.thenThrow(new UserAlreadyExistsException("User already exists"));

			// When & Then
			try {
				userController.signup(signupDto);
			} catch (UserAlreadyExistsException e) {
				assertEquals("User already exists", e.getMessage());
			}

			verify(userMapper, times(1)).toModel(signupDto);
			verify(userService, times(1)).registerUser(userModel);
		}
	}

	@Nested
	@DisplayName("User Login Tests")
	class UserLoginTests {

		@Test
		@DisplayName("Should successfully login user with valid credentials")
		void shouldLoginUserSuccessfully() {
			// Given
			LoginDto loginDto =
					LoginDtoTestDataBuilder.aLoginDto()
							.withEmail(TEST_EMAIL)
							.withPassword(TEST_PASSWORD)
							.build();

			UserDto expectedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withEmail(TEST_EMAIL)
							.withJwtToken("valid.jwt.token")
							.build();

			when(userService.loginUser(loginDto)).thenReturn(expectedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response = userController.login(loginDto);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(expectedUserDto, Objects.requireNonNull(response.getBody()).getData());

			verify(userService, times(1)).loginUser(loginDto);
		}

		@Test
		@DisplayName("Should handle invalid credentials exception during login")
		void shouldHandleInvalidCredentialsException() {
			// Given
			LoginDto loginDto =
					LoginDtoTestDataBuilder.aLoginDto()
							.withEmail(TEST_EMAIL)
							.withPassword("wrongpassword")
							.build();

			when(userService.loginUser(loginDto))
					.thenThrow(new RuntimeException("Invalid credentials"));

			// When & Then
			try {
				userController.login(loginDto);
			} catch (RuntimeException e) {
				assertEquals("Invalid credentials", e.getMessage());
			}

			verify(userService, times(1)).loginUser(loginDto);
		}
	}

	@Nested
	@DisplayName("User Search Tests")
	class UserSearchTests {

		@Test
		@DisplayName("Should successfully search users with valid query")
		void shouldSearchUsersSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			List<UserDto> searchResults =
					List.of(
							UserDtoTestDataBuilder.aUserDto()
									.withId(OTHER_USER_ID)
									.withName("John Doe")
									.withEmail("john@example.com")
									.build(),
							UserDtoTestDataBuilder.aUserDto()
									.withId(UUID.randomUUID())
									.withName("Jane Smith")
									.withEmail("jane@example.com")
									.build());

			when(userService.searchUsers(SEARCH_QUERY, TEST_USER_ID)).thenReturn(searchResults);

			// When
			ResponseEntity<ApiResponse<List<UserDto>>> response =
					userController.searchUsers(SEARCH_QUERY, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(searchResults, Objects.requireNonNull(response.getBody()).getData());
			assertEquals(2, Objects.requireNonNull(response.getBody()).getData().size());

			verify(userService, times(1)).searchUsers(SEARCH_QUERY, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should return empty list when no users match search query")
		void shouldReturnEmptyListWhenNoUsersMatch() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			String nonMatchingQuery = "nonexistentuser";
			List<UserDto> emptyResults = List.of();

			when(userService.searchUsers(nonMatchingQuery, TEST_USER_ID)).thenReturn(emptyResults);

			// When
			ResponseEntity<ApiResponse<List<UserDto>>> response =
					userController.searchUsers(nonMatchingQuery, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(emptyResults, Objects.requireNonNull(response.getBody()).getData());
			assertEquals(0, Objects.requireNonNull(response.getBody()).getData().size());

			verify(userService, times(1)).searchUsers(nonMatchingQuery, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Get User By ID Tests")
	class GetUserByIdTests {

		@Test
		@DisplayName("Should successfully get user by valid ID")
		void shouldGetUserByIdSuccessfully() {
			// Given
			UserDto expectedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withName(TEST_NAME)
							.withEmail(TEST_EMAIL)
							.build();

			when(userService.getUserById(TEST_USER_ID)).thenReturn(expectedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.getUserById(TEST_USER_ID);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(expectedUserDto, Objects.requireNonNull(response.getBody()).getData());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle user not found exception")
		void shouldHandleUserNotFoundException() {
			// Given
			UUID nonExistentUserId = UUID.randomUUID();

			when(userService.getUserById(nonExistentUserId))
					.thenThrow(new UserNotFoundException("User not found"));

			// When & Then
			try {
				userController.getUserById(nonExistentUserId);
			} catch (UserNotFoundException e) {
				assertEquals("User not found", e.getMessage());
			}

			verify(userService, times(1)).getUserById(nonExistentUserId);
		}
	}

	@Nested
	@DisplayName("Update Current User Tests")
	class UpdateCurrentUserTests {

		@Test
		@DisplayName("Should successfully update current user profile")
		void shouldUpdateCurrentUserSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			UserUpdateDto updateDto =
					UserUpdateDtoTestDataBuilder.aUserUpdateDto()
							.withName("Updated Name")
							.withPhone("+9876543210")
							.build();

			UserDto updatedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withName("Updated Name")
							.withPhone("+9876543210")
							.withEmail(TEST_EMAIL)
							.build();

			when(userService.updateUser(TEST_USER_ID, updateDto)).thenReturn(updatedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.updateCurrentUser(currentUser, updateDto);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(updatedUserDto, Objects.requireNonNull(response.getBody()).getData());

			verify(userService, times(1)).updateUser(TEST_USER_ID, updateDto);
		}

		@Test
		@DisplayName("Should handle service exception during user update")
		void shouldHandleServiceExceptionDuringUpdate() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			UserUpdateDto updateDto =
					UserUpdateDtoTestDataBuilder.aUserUpdateDto().withName("Updated Name").build();

			when(userService.updateUser(TEST_USER_ID, updateDto))
					.thenThrow(new RuntimeException("Update failed"));

			// When & Then
			try {
				userController.updateCurrentUser(currentUser, updateDto);
			} catch (RuntimeException e) {
				assertEquals("Update failed", e.getMessage());
			}

			verify(userService, times(1)).updateUser(TEST_USER_ID, updateDto);
		}
	}

	@Nested
	@DisplayName("Upload Profile Picture Tests")
	class UploadProfilePictureTests {

		@Test
		@DisplayName("Should successfully upload valid profile picture")
		void shouldUploadProfilePictureSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockMultipartFile file =
					new MockMultipartFile(
							"file", "profile.jpg", "image/jpeg", "test image content".getBytes());

			UserDto updatedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture("profile.jpg")
							.build();

			when(userService.updateProfilePicture(TEST_USER_ID, file)).thenReturn(updatedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.uploadProfilePicture(currentUser, file);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(updatedUserDto, Objects.requireNonNull(response.getBody()).getData());

			verify(userService, times(1)).updateProfilePicture(TEST_USER_ID, file);
		}

		@Test
		@DisplayName("Should reject empty file upload")
		void shouldRejectEmptyFile() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockMultipartFile emptyFile =
					new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.uploadProfilePicture(currentUser, emptyFile);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.BAD_REQUEST.value(),
					Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(
					"File cannot be empty", Objects.requireNonNull(response.getBody()).getError());

			verifyNoInteractions(userService);
		}

		@Test
		@DisplayName("Should reject file exceeding size limit")
		void shouldRejectOversizedFile() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB - exceeds 5MB limit
			MockMultipartFile largeFile =
					new MockMultipartFile("file", "large.jpg", "image/jpeg", largeContent);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.uploadProfilePicture(currentUser, largeFile);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.BAD_REQUEST.value(),
					Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(
					"File size exceeds limit",
					Objects.requireNonNull(response.getBody()).getError());

			verifyNoInteractions(userService);
		}

		@Test
		@DisplayName("Should handle service exception during file upload")
		void shouldHandleServiceExceptionDuringUpload() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockMultipartFile file =
					new MockMultipartFile(
							"file", "profile.jpg", "image/jpeg", "test content".getBytes());

			when(userService.updateProfilePicture(TEST_USER_ID, file))
					.thenThrow(new RuntimeException("Upload failed"));

			// When & Then
			try {
				userController.uploadProfilePicture(currentUser, file);
			} catch (RuntimeException e) {
				assertEquals("Upload failed", e.getMessage());
			}

			verify(userService, times(1)).updateProfilePicture(TEST_USER_ID, file);
		}
	}

	@Nested
	@DisplayName("Get Current User Profile Picture Tests")
	class GetCurrentUserProfilePictureTests {

		@Test
		@DisplayName("Should successfully return profile picture when exists")
		void shouldReturnProfilePictureSuccessfully() throws MalformedURLException, IOException {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockHttpServletRequest request = new MockHttpServletRequest();

			UserDto userDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture(TEST_FILENAME)
							.build();

			when(userService.getUserById(TEST_USER_ID)).thenReturn(userDto);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(mockResource);
			when(mockResource.getFile()).thenReturn(new java.io.File("profile.jpg"));

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(currentUser, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(mockResource, response.getBody());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
			verify(fileStorage, times(1))
					.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}

		@Test
		@DisplayName("Should return unauthorized when user is null")
		void shouldReturnUnauthorizedWhenUserIsNull() {
			// Given
			MockHttpServletRequest request = new MockHttpServletRequest();

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(null, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

			verifyNoInteractions(userService, fileStorage);
		}

		@Test
		@DisplayName("Should return not found when profile picture filename is null")
		void shouldReturnNotFoundWhenFilenameIsNull() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockHttpServletRequest request = new MockHttpServletRequest();

			UserDto userDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture(null)
							.build();

			when(userService.getUserById(TEST_USER_ID)).thenReturn(userDto);

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(currentUser, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
			verifyNoInteractions(fileStorage);
		}

		@Test
		@DisplayName("Should return not found when profile picture filename is blank")
		void shouldReturnNotFoundWhenFilenameIsBlank() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockHttpServletRequest request = new MockHttpServletRequest();

			UserDto userDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture("")
							.build();

			when(userService.getUserById(TEST_USER_ID)).thenReturn(userDto);

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(currentUser, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
			verifyNoInteractions(fileStorage);
		}

		@Test
		@DisplayName("Should return internal server error when MalformedURLException occurs")
		void shouldReturnInternalServerErrorOnMalformedURLException() throws MalformedURLException {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockHttpServletRequest request = new MockHttpServletRequest();

			UserDto userDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture(TEST_FILENAME)
							.build();

			when(userService.getUserById(TEST_USER_ID)).thenReturn(userDto);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenThrow(new MalformedURLException("Invalid URL"));

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(currentUser, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
			verify(fileStorage, times(1))
					.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}

		@Test
		@DisplayName("Should return not found when RuntimeException occurs")
		void shouldReturnNotFoundOnRuntimeException() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockHttpServletRequest request = new MockHttpServletRequest();

			when(userService.getUserById(TEST_USER_ID))
					.thenThrow(new RuntimeException("User service error"));

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(currentUser, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
			verifyNoInteractions(fileStorage);
		}

		@Test
		@DisplayName("Should use default content type when unable to determine from file")
		void shouldUseDefaultContentTypeWhenUnableToDetermine()
				throws MalformedURLException, IOException {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockHttpServletRequest request = new MockHttpServletRequest();

			UserDto userDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture(TEST_FILENAME)
							.build();

			when(userService.getUserById(TEST_USER_ID)).thenReturn(userDto);
			when(fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME))
					.thenReturn(mockResource);
			when(mockResource.getFile()).thenThrow(new IOException("File access error"));

			// When
			ResponseEntity<Resource> response =
					userController.getCurrentUserProfilePicture(currentUser, request);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertEquals(mockResource, response.getBody());

			verify(userService, times(1)).getUserById(TEST_USER_ID);
			verify(fileStorage, times(1))
					.loadFileAsResource(FolderName.PROFILE_PICTURES, TEST_FILENAME);
		}
	}

	@Nested
	@DisplayName("File Upload Validation Tests")
	class FileUploadValidationTests {

		@Test
		@DisplayName("Should accept file at exactly size limit")
		void shouldAcceptFileAtSizeLimit() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			byte[] exactLimitContent = new byte[5 * 1024 * 1024]; // Exactly 5MB
			MockMultipartFile file =
					new MockMultipartFile("file", "limit.jpg", "image/jpeg", exactLimitContent);

			UserDto updatedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture("limit.jpg")
							.build();

			when(userService.updateProfilePicture(TEST_USER_ID, file)).thenReturn(updatedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.uploadProfilePicture(currentUser, file);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());

			verify(userService, times(1)).updateProfilePicture(TEST_USER_ID, file);
		}

		@Test
		@DisplayName("Should accept very small valid file")
		void shouldAcceptSmallValidFile() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			MockMultipartFile smallFile =
					new MockMultipartFile(
							"file", "small.jpg", "image/jpeg", "x".getBytes() // 1 byte
							);

			UserDto updatedUserDto =
					UserDtoTestDataBuilder.aUserDto()
							.withId(TEST_USER_ID)
							.withProfilePicture("small.jpg")
							.build();

			when(userService.updateProfilePicture(TEST_USER_ID, smallFile))
					.thenReturn(updatedUserDto);

			// When
			ResponseEntity<ApiResponse<UserDto>> response =
					userController.uploadProfilePicture(currentUser, smallFile);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());

			verify(userService, times(1)).updateProfilePicture(TEST_USER_ID, smallFile);
		}
	}
}
