/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service.impl;

import static com.ah.whatsapp.dto.LoginDtoTestDataBuilder.aLoginDto;
import static com.ah.whatsapp.dto.UserDtoTestDataBuilder.aUserDto;
import static com.ah.whatsapp.dto.UserUpdateDtoTestDataBuilder.aUserUpdateDto;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserUpdateDto;
import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.exception.InvalidCredentialsException;
import com.ah.whatsapp.exception.UserAlreadyExistsException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.FileStorage;
import com.ah.whatsapp.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

	@Mock private UserRepository userRepository;

	@Mock private UserMapper userMapper;

	@Mock private JwtUtil jwtUtil;

	@Mock private PasswordEncoder passwordEncoder;

	@Mock private AuthenticationManager authenticationManager;

	@Mock private FileStorage fileStorage;

	@InjectMocks private UserServiceImpl userService;

	private User testUser;
	private UserDto testUserDto;
	private UUID testUserId;
	private String testEmail;
	private String testJwtToken;

	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID();
		testEmail = "test@example.com";
		testJwtToken = "test.jwt.token";
		testUser = aUser().withId(testUserId).withEmail(testEmail).build();
		testUserDto =
				aUserDto()
						.withId(testUserId)
						.withEmail(testEmail)
						.withJwtToken(testJwtToken)
						.build();
	}

	@Nested
	@DisplayName("Save User Tests")
	class SaveUserTests {

		@Test
		@DisplayName("Should save user successfully")
		void shouldSaveUserSuccessfully() {
			// Given
			when(userRepository.save(testUser)).thenReturn(testUser);

			// When
			User result = userService.save(testUser);

			// Then
			assertNotNull(result);
			assertEquals(testUser, result);
			verify(userRepository).save(testUser);
		}
	}

	@Nested
	@DisplayName("Register User Tests")
	class RegisterUserTests {

		@Test
		@DisplayName("Should register new user successfully")
		void shouldRegisterNewUserSuccessfully() {
			// Given
			String rawPassword = "password123";
			String encodedPassword = "encoded.password";
			User userToRegister = aUser().withEmail(testEmail).withPassword(rawPassword).build();

			when(userRepository.existsByEmail(testEmail)).thenReturn(false);
			when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
			when(userRepository.save(any(User.class))).thenReturn(testUser);
			when(jwtUtil.generateToken(testEmail)).thenReturn(testJwtToken);
			when(userMapper.toDto(testUser, testJwtToken)).thenReturn(testUserDto);

			// When
			UserDto result = userService.registerUser(userToRegister);

			// Then
			assertNotNull(result);
			assertEquals(testUserDto, result);

			// Verify password was encoded
			ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userCaptor.capture());
			assertEquals(encodedPassword, userCaptor.getValue().getPassword());

			verify(userRepository).existsByEmail(testEmail);
			verify(passwordEncoder).encode(rawPassword);
			verify(jwtUtil).generateToken(testEmail);
			verify(userMapper).toDto(testUser, testJwtToken);
		}

		@Test
		@DisplayName("Should throw exception when user already exists")
		void shouldThrowExceptionWhenUserAlreadyExists() {
			// Given
			User userToRegister = aUser().withEmail(testEmail).build();
			when(userRepository.existsByEmail(testEmail)).thenReturn(true);

			// When & Then
			UserAlreadyExistsException exception =
					assertThrows(
							UserAlreadyExistsException.class,
							() -> userService.registerUser(userToRegister));

			assertEquals(
					"User with email " + testEmail + " already exists.", exception.getMessage());
			verify(userRepository).existsByEmail(testEmail);
			verify(userRepository, never()).save(any());
			verify(passwordEncoder, never()).encode(any());
		}
	}

	@Nested
	@DisplayName("Login User Tests")
	class LoginUserTests {

		@Test
		@DisplayName("Should login user successfully")
		void shouldLoginUserSuccessfully() {
			// Given
			LoginDto loginDto =
					aLoginDto().withEmail(testEmail).withPassword("password123").build();

			when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
			when(jwtUtil.generateToken(testEmail)).thenReturn(testJwtToken);
			when(userMapper.toDto(testUser, testJwtToken)).thenReturn(testUserDto);

			// When
			UserDto result = userService.loginUser(loginDto);

			// Then
			assertNotNull(result);
			assertEquals(testUserDto, result);

			verify(userRepository).findByEmail(testEmail);
			verify(authenticationManager)
					.authenticate(
							new UsernamePasswordAuthenticationToken(testEmail, "password123"));
			verify(jwtUtil).generateToken(testEmail);
			verify(userMapper).toDto(testUser, testJwtToken);
		}

		@Test
		@DisplayName("Should throw exception when user not found")
		void shouldThrowExceptionWhenUserNotFound() {
			// Given
			LoginDto loginDto = aLoginDto().withEmail(testEmail).build();
			when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

			// When & Then
			UsernameNotFoundException exception =
					assertThrows(
							UsernameNotFoundException.class, () -> userService.loginUser(loginDto));

			assertEquals("User not found with email: " + testEmail, exception.getMessage());
			verify(userRepository).findByEmail(testEmail);
			verify(authenticationManager, never()).authenticate(any());
		}

		@Test
		@DisplayName("Should throw exception when authentication fails")
		void shouldThrowExceptionWhenAuthenticationFails() {
			// Given
			LoginDto loginDto =
					aLoginDto().withEmail(testEmail).withPassword("wrongpassword").build();
			when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
			when(authenticationManager.authenticate(any()))
					.thenThrow(new BadCredentialsException("Bad credentials"));

			// When & Then
			InvalidCredentialsException exception =
					assertThrows(
							InvalidCredentialsException.class,
							() -> userService.loginUser(loginDto));

			assertEquals("Invalid credentials", exception.getMessage());
			verify(userRepository).findByEmail(testEmail);
			verify(authenticationManager).authenticate(any());
		}
	}

	@Nested
	@DisplayName("Get User By Id Tests")
	class GetUserByIdTests {

		@Test
		@DisplayName("Should get user by id successfully")
		void shouldGetUserByIdSuccessfully() {
			// Given
			when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
			when(userMapper.toDto(testUser)).thenReturn(testUserDto);

			// When
			UserDto result = userService.getUserById(testUserId);

			// Then
			assertNotNull(result);
			assertEquals(testUserDto, result);
			verify(userRepository).findById(testUserId);
			verify(userMapper).toDto(testUser);
		}

		@Test
		@DisplayName("Should throw exception when user not found")
		void shouldThrowExceptionWhenUserNotFound() {
			// Given
			when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class, () -> userService.getUserById(testUserId));

			assertEquals("User not found with id: " + testUserId, exception.getMessage());
			verify(userRepository).findById(testUserId);
			verify(userMapper, never()).toDto(any(User.class));
		}
	}

	@Nested
	@DisplayName("Search Users Tests")
	class SearchUsersTests {

		@Test
		@DisplayName("Should search users successfully")
		void shouldSearchUsersSuccessfully() {
			// Given
			String query = "john";
			UUID excludeUserId = UUID.randomUUID();
			List<User> users = List.of(testUser);
			List<UserDto> userDtos = List.of(testUserDto);

			when(userRepository.searchUsers(query, excludeUserId)).thenReturn(users);
			when(userMapper.toDto(testUser)).thenReturn(testUserDto);

			// When
			List<UserDto> result = userService.searchUsers(query, excludeUserId);

			// Then
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(userDtos, result);
			verify(userRepository).searchUsers(query, excludeUserId);
			verify(userMapper).toDto(testUser);
		}

		@Test
		@DisplayName("Should return empty list when query is null")
		void shouldReturnEmptyListWhenQueryIsNull() {
			// When
			List<UserDto> result = userService.searchUsers(null, UUID.randomUUID());

			// Then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(userRepository, never()).searchUsers(any(), any());
		}

		@Test
		@DisplayName("Should return empty list when query is empty")
		void shouldReturnEmptyListWhenQueryIsEmpty() {
			// When
			List<UserDto> result = userService.searchUsers("", UUID.randomUUID());

			// Then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(userRepository, never()).searchUsers(any(), any());
		}

		@Test
		@DisplayName("Should trim query before searching")
		void shouldTrimQueryBeforeSearching() {
			// Given
			String query = "  john  ";
			UUID excludeUserId = UUID.randomUUID();
			when(userRepository.searchUsers("john", excludeUserId)).thenReturn(List.of());

			// When
			userService.searchUsers(query, excludeUserId);

			// Then
			verify(userRepository).searchUsers("john", excludeUserId);
		}
	}

	@Nested
	@DisplayName("Exists By Id Tests")
	class ExistsByIdTests {

		@Test
		@DisplayName("Should return true when user exists")
		void shouldReturnTrueWhenUserExists() {
			// Given
			when(userRepository.existsById(testUserId)).thenReturn(true);

			// When
			Boolean result = userService.existsById(testUserId);

			// Then
			assertTrue(result);
			verify(userRepository).existsById(testUserId);
		}

		@Test
		@DisplayName("Should return false when user does not exist")
		void shouldReturnFalseWhenUserDoesNotExist() {
			// Given
			when(userRepository.existsById(testUserId)).thenReturn(false);

			// When
			Boolean result = userService.existsById(testUserId);

			// Then
			assertFalse(result);
			verify(userRepository).existsById(testUserId);
		}
	}

	@Nested
	@DisplayName("Update User Tests")
	class UpdateUserTests {

		@Test
		@DisplayName("Should update user name and phone successfully")
		void shouldUpdateUserNameAndPhoneSuccessfully() {
			// Given
			UserUpdateDto updateDto =
					aUserUpdateDto().withName("New Name").withPhone("+9876543210").build();

			User existingUser =
					aUser().withId(testUserId)
							.withName("Old Name")
							.withPhone("+1234567890")
							.build();
			User updatedUser =
					aUser().withId(testUserId)
							.withName("New Name")
							.withPhone("+9876543210")
							.build();

			when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
			when(userRepository.save(any(User.class))).thenReturn(updatedUser);
			when(userMapper.toDto(updatedUser)).thenReturn(testUserDto);

			// When
			UserDto result = userService.updateUser(testUserId, updateDto);

			// Then
			assertNotNull(result);
			assertEquals(testUserDto, result);

			ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userCaptor.capture());
			User savedUser = userCaptor.getValue();
			assertEquals("New Name", savedUser.getName());
			assertEquals("+9876543210", savedUser.getPhone());
			assertNotNull(savedUser.getUpdatedAt());

			verify(userRepository).findById(testUserId);
			verify(userMapper).toDto(updatedUser);
		}

		@Test
		@DisplayName("Should update only name when phone is same")
		void shouldUpdateOnlyNameWhenPhoneIsSame() {
			// Given
			UserUpdateDto updateDto =
					aUserUpdateDto().withName("New Name").withPhone("+1234567890").build();

			User existingUser =
					aUser().withId(testUserId)
							.withName("Old Name")
							.withPhone("+1234567890")
							.build();

			when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
			when(userRepository.save(any(User.class))).thenReturn(existingUser);
			when(userMapper.toDto(existingUser)).thenReturn(testUserDto);

			// When
			UserDto result = userService.updateUser(testUserId, updateDto);

			// Then
			assertNotNull(result);

			ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userCaptor.capture());
			User savedUser = userCaptor.getValue();
			assertEquals("New Name", savedUser.getName());
			assertEquals("+1234567890", savedUser.getPhone());
		}

		@Test
		@DisplayName("Should not update when no changes")
		void shouldNotUpdateWhenNoChanges() {
			// Given
			UserUpdateDto updateDto =
					aUserUpdateDto().withName("Same Name").withPhone("+1234567890").build();

			User existingUser =
					aUser().withId(testUserId)
							.withName("Same Name")
							.withPhone("+1234567890")
							.build();

			when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
			when(userMapper.toDto(existingUser)).thenReturn(testUserDto);

			// When
			UserDto result = userService.updateUser(testUserId, updateDto);

			// Then
			assertNotNull(result);
			verify(userRepository).findById(testUserId);
			verify(userRepository, never()).save(any());
			verify(userMapper).toDto(existingUser);
		}

		@Test
		@DisplayName("Should throw exception when user not found")
		void shouldThrowExceptionWhenUserNotFound() {
			// Given
			UserUpdateDto updateDto = aUserUpdateDto().build();
			when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class,
							() -> userService.updateUser(testUserId, updateDto));

			assertEquals("User not found with id: " + testUserId, exception.getMessage());
			verify(userRepository).findById(testUserId);
			verify(userRepository, never()).save(any());
		}

		@Test
		@DisplayName("Should handle null values in update dto")
		void shouldHandleNullValuesInUpdateDto() {
			// Given
			UserUpdateDto updateDto = aUserUpdateDto().withNullValues().build();
			User existingUser = aUser().withId(testUserId).build();

			when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
			when(userMapper.toDto(existingUser)).thenReturn(testUserDto);

			// When
			UserDto result = userService.updateUser(testUserId, updateDto);

			// Then
			assertNotNull(result);
			verify(userRepository).findById(testUserId);
			verify(userRepository, never()).save(any());
			verify(userMapper).toDto(existingUser);
		}
	}

	@Nested
	@DisplayName("Update Profile Picture Tests")
	class UpdateProfilePictureTests {

		@Mock private MultipartFile mockFile;

		@Test
		@DisplayName("Should update profile picture successfully")
		void shouldUpdateProfilePictureSuccessfully() throws IOException {
			// Given
			String filename = "profile-picture.jpg";
			User existingUser = aUser().withId(testUserId).build();
			User updatedUser = aUser().withId(testUserId).withProfilePicture(filename).build();

			when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
			when(fileStorage.storeFile(
							mockFile, FolderName.PROFILE_PICTURES, testUserId.toString()))
					.thenReturn(filename);
			when(userRepository.save(any(User.class))).thenReturn(updatedUser);
			when(userMapper.toDto(updatedUser)).thenReturn(testUserDto);

			// When
			UserDto result = userService.updateProfilePicture(testUserId, mockFile);

			// Then
			assertNotNull(result);
			assertEquals(testUserDto, result);

			ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userCaptor.capture());
			User savedUser = userCaptor.getValue();
			assertEquals(filename, savedUser.getProfilePicture());
			assertNotNull(savedUser.getUpdatedAt());

			verify(userRepository).findById(testUserId);
			verify(fileStorage)
					.storeFile(mockFile, FolderName.PROFILE_PICTURES, testUserId.toString());
			verify(userMapper).toDto(updatedUser);
		}

		@Test
		@DisplayName("Should throw exception when user not found")
		void shouldThrowExceptionWhenUserNotFound() {
			// Given
			when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class,
							() -> userService.updateProfilePicture(testUserId, mockFile));

			assertEquals("User not found with id: " + testUserId, exception.getMessage());
			verify(userRepository).findById(testUserId);
			verifyNoInteractions(fileStorage);
			verify(userRepository, never()).save(any());
		}

		@Test
		@DisplayName("Should throw runtime exception when file storage fails")
		void shouldThrowRuntimeExceptionWhenFileStorageFails() throws IOException {
			// Given
			User existingUser = aUser().withId(testUserId).build();
			IOException ioException = new IOException("Storage failed");

			when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
			when(fileStorage.storeFile(
							mockFile, FolderName.PROFILE_PICTURES, testUserId.toString()))
					.thenThrow(ioException);

			// When & Then
			RuntimeException exception =
					assertThrows(
							RuntimeException.class,
							() -> userService.updateProfilePicture(testUserId, mockFile));

			assertEquals(
					"Could not store profile picture for user "
							+ testUserId
							+ ". Please try again!",
					exception.getMessage());
			assertEquals(ioException, exception.getCause());

			verify(userRepository).findById(testUserId);
			verify(fileStorage)
					.storeFile(mockFile, FolderName.PROFILE_PICTURES, testUserId.toString());
			verify(userRepository, never()).save(any());
		}
	}
}
