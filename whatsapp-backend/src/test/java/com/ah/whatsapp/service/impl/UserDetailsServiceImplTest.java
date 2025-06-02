/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service.impl;

import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;

/**
 * Comprehensive Unit Tests for UserDetailsServiceImpl
 * Following industry best practices for Spring Security UserDetailsService testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

	@Mock private UserRepository userRepository;

	@InjectMocks private UserDetailsServiceImpl userDetailsService;

	private User testUser;
	private UUID testUserId;
	private String testEmail;
	private String testPassword;

	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID();
		testEmail = "test@example.com";
		testPassword = "encodedPassword123";
		testUser =
				aUser().withId(testUserId)
						.withEmail(testEmail)
						.withPassword(testPassword)
						.withName("Test User")
						.withPhone("+1234567890")
						.build();
	}

	@Nested
	@DisplayName("Load User By Username Tests")
	class LoadUserByUsernameTests {

		@Test
		@DisplayName("Should load user successfully when user exists")
		void shouldLoadUserSuccessfullyWhenUserExists() {
			// Given
			when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

			// When
			UserDetails result = userDetailsService.loadUserByUsername(testEmail);

			// Then
			assertNotNull(result);
			assertTrue(result instanceof JwtUser);

			JwtUser jwtUser = (JwtUser) result;
			assertEquals(testEmail, jwtUser.getUsername());
			assertEquals(testUserId, jwtUser.getUserId());
			assertEquals(testPassword, jwtUser.getPassword());
			assertTrue(jwtUser.getAuthorities().isEmpty());

			verify(userRepository).findByEmail(testEmail);
		}

		@Test
		@DisplayName("Should throw UsernameNotFoundException when user not found")
		void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
			// Given
			String nonExistentEmail = "nonexistent@example.com";
			when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

			// When & Then
			UsernameNotFoundException exception =
					assertThrows(
							UsernameNotFoundException.class,
							() -> userDetailsService.loadUserByUsername(nonExistentEmail));

			assertEquals("User not found with email: " + nonExistentEmail, exception.getMessage());
			verify(userRepository).findByEmail(nonExistentEmail);
		}

		@Test
		@DisplayName("Should handle null username gracefully")
		void shouldHandleNullUsernameGracefully() {
			// Given
			String nullEmail = null;
			when(userRepository.findByEmail(nullEmail)).thenReturn(Optional.empty());

			// When & Then
			UsernameNotFoundException exception =
					assertThrows(
							UsernameNotFoundException.class,
							() -> userDetailsService.loadUserByUsername(nullEmail));

			assertEquals("User not found with email: null", exception.getMessage());
			verify(userRepository).findByEmail(nullEmail);
		}

		@Test
		@DisplayName("Should handle empty username gracefully")
		void shouldHandleEmptyUsernameGracefully() {
			// Given
			String emptyEmail = "";
			when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

			// When & Then
			UsernameNotFoundException exception =
					assertThrows(
							UsernameNotFoundException.class,
							() -> userDetailsService.loadUserByUsername(emptyEmail));

			assertEquals("User not found with email: ", exception.getMessage());
			verify(userRepository).findByEmail(emptyEmail);
		}

		@Test
		@DisplayName("Should handle whitespace-only username gracefully")
		void shouldHandleWhitespaceOnlyUsernameGracefully() {
			// Given
			String whitespaceEmail = "   ";
			when(userRepository.findByEmail(whitespaceEmail)).thenReturn(Optional.empty());

			// When & Then
			UsernameNotFoundException exception =
					assertThrows(
							UsernameNotFoundException.class,
							() -> userDetailsService.loadUserByUsername(whitespaceEmail));

			assertEquals("User not found with email: " + whitespaceEmail, exception.getMessage());
			verify(userRepository).findByEmail(whitespaceEmail);
		}

		@Test
		@DisplayName("Should load user with different email formats")
		void shouldLoadUserWithDifferentEmailFormats() {
			// Test various valid email formats
			String[] validEmails = {
				"user@domain.com",
				"user.name@domain.com",
				"user+tag@domain.com",
				"user123@sub.domain.com",
				"USER@DOMAIN.COM"
			};

			for (String email : validEmails) {
				// Given
				User userWithEmail = aUser().withEmail(email).build();
				when(userRepository.findByEmail(email)).thenReturn(Optional.of(userWithEmail));

				// When
				UserDetails result = userDetailsService.loadUserByUsername(email);

				// Then
				assertNotNull(result);
				assertEquals(email, result.getUsername());

				verify(userRepository).findByEmail(email);
			}
		}

		@Test
		@DisplayName("Should create JwtUser with correct properties from User")
		void shouldCreateJwtUserWithCorrectPropertiesFromUser() {
			// Given
			User userWithSpecificData =
					aUser().withId(testUserId)
							.withEmail("specific@example.com")
							.withPassword("specificPassword")
							.withName("Specific User")
							.withPhone("+9876543210")
							.build();

			when(userRepository.findByEmail("specific@example.com"))
					.thenReturn(Optional.of(userWithSpecificData));

			// When
			UserDetails result = userDetailsService.loadUserByUsername("specific@example.com");

			// Then
			assertNotNull(result);
			assertTrue(result instanceof JwtUser);

			JwtUser jwtUser = (JwtUser) result;
			assertEquals("specific@example.com", jwtUser.getUsername());
			assertEquals(testUserId, jwtUser.getUserId());
			assertEquals("specificPassword", jwtUser.getPassword());
			assertTrue(jwtUser.getAuthorities().isEmpty());

			// Verify JwtUser implements UserDetails correctly
			assertTrue(jwtUser.isAccountNonExpired());
			assertTrue(jwtUser.isAccountNonLocked());
			assertTrue(jwtUser.isCredentialsNonExpired());
			assertTrue(jwtUser.isEnabled());

			verify(userRepository).findByEmail("specific@example.com");
		}

		@Test
		@DisplayName("Should handle user with null password")
		void shouldHandleUserWithNullPassword() {
			// Given
			User userWithNullPassword =
					aUser().withId(testUserId).withEmail(testEmail).withPassword(null).build();

			when(userRepository.findByEmail(testEmail))
					.thenReturn(Optional.of(userWithNullPassword));

			// When
			UserDetails result = userDetailsService.loadUserByUsername(testEmail);

			// Then
			assertNotNull(result);
			assertTrue(result instanceof JwtUser);

			JwtUser jwtUser = (JwtUser) result;
			assertEquals(testEmail, jwtUser.getUsername());
			assertEquals(testUserId, jwtUser.getUserId());
			assertEquals(null, jwtUser.getPassword());

			verify(userRepository).findByEmail(testEmail);
		}
	}

	@Nested
	@DisplayName("Service Integration Tests")
	class ServiceIntegrationTests {

		@Test
		@DisplayName("Should work correctly with Spring Security authentication flow")
		void shouldWorkCorrectlyWithSpringSecurityAuthenticationFlow() {
			// Given
			String authenticatingEmail = "auth@example.com";
			String authenticatingPassword = "securePassword";
			User authenticatingUser =
					aUser().withEmail(authenticatingEmail)
							.withPassword(authenticatingPassword)
							.build();

			when(userRepository.findByEmail(authenticatingEmail))
					.thenReturn(Optional.of(authenticatingUser));

			// When
			UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatingEmail);

			// Then
			assertNotNull(userDetails);
			assertEquals(authenticatingEmail, userDetails.getUsername());
			assertEquals(authenticatingPassword, userDetails.getPassword());

			// Verify all UserDetails contract methods work correctly
			assertNotNull(userDetails.getAuthorities());
			assertTrue(userDetails.isAccountNonExpired());
			assertTrue(userDetails.isAccountNonLocked());
			assertTrue(userDetails.isCredentialsNonExpired());
			assertTrue(userDetails.isEnabled());

			verify(userRepository).findByEmail(authenticatingEmail);
		}

		@Test
		@DisplayName("Should maintain consistency across multiple calls for same user")
		void shouldMaintainConsistencyAcrossMultipleCallsForSameUser() {
			// Given
			when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

			// When - multiple calls
			UserDetails firstCall = userDetailsService.loadUserByUsername(testEmail);
			UserDetails secondCall = userDetailsService.loadUserByUsername(testEmail);

			// Then - both calls should return equivalent results
			assertEquals(firstCall.getUsername(), secondCall.getUsername());
			assertEquals(firstCall.getPassword(), secondCall.getPassword());
			assertEquals(((JwtUser) firstCall).getUserId(), ((JwtUser) secondCall).getUserId());

			verify(userRepository, times(2)).findByEmail(testEmail);
		}
	}

	@Nested
	@DisplayName("Error Handling Tests")
	class ErrorHandlingTests {

		@Test
		@DisplayName("Should provide meaningful error message for non-existent user")
		void shouldProvideMeaningfulErrorMessageForNonExistentUser() {
			// Given
			String nonExistentEmail = "missing@example.com";
			when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

			// When & Then
			UsernameNotFoundException exception =
					assertThrows(
							UsernameNotFoundException.class,
							() -> userDetailsService.loadUserByUsername(nonExistentEmail));

			assertTrue(exception.getMessage().contains(nonExistentEmail));
			assertTrue(exception.getMessage().contains("User not found with email"));
			verify(userRepository).findByEmail(nonExistentEmail);
		}

		@Test
		@DisplayName("Should handle repository exceptions gracefully")
		void shouldHandleRepositoryExceptionsGracefully() {
			// Given
			String testEmailForException = "exception@example.com";
			when(userRepository.findByEmail(testEmailForException))
					.thenThrow(new RuntimeException("Database connection error"));

			// When & Then
			RuntimeException exception =
					assertThrows(
							RuntimeException.class,
							() -> userDetailsService.loadUserByUsername(testEmailForException));

			assertEquals("Database connection error", exception.getMessage());
			verify(userRepository).findByEmail(testEmailForException);
		}
	}

	@Nested
	@DisplayName("Constructor and Dependency Injection Tests")
	class ConstructorAndDependencyInjectionTests {

		@Test
		@DisplayName("Should be properly initialized with UserRepository dependency")
		void shouldBeProperlyInitializedWithUserRepositoryDependency() {
			// Given
			UserRepository mockRepository = userRepository;

			// When
			UserDetailsServiceImpl service = new UserDetailsServiceImpl(mockRepository);

			// Then
			assertNotNull(service);
			// The service should be ready to use after proper dependency injection
		}
	}
}
