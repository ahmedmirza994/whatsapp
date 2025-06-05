/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.integration.BaseIntegrationTest;
import com.ah.whatsapp.testutil.TestDataFactory;

/**
 * Integration tests for UserEntityRepository using TestContainers.
 * Tests actual database interactions with PostgreSQL container.
 *
 * <p>These tests verify:
 * - Basic CRUD operations
 * - Custom query methods
 * - Search functionality
 * - Database constraints and behavior
 */
@DisplayName("UserEntityRepository Integration Tests")
class UserEntityRepositoryIntegrationTest extends BaseIntegrationTest {

	@Autowired private UserEntityRepository userRepository;

	private UserEntity testUser1;
	private UserEntity testUser2;
	private UserEntity testUser3;

	@BeforeEach
	void setUp() {
		// Clean up database before each test
		userRepository.deleteAll();

		// Create test users using TestDataFactory
		testUser1 =
				TestDataFactory.createTestUser("John Doe", "john.doe@example.com", "+1234567890");
		testUser2 =
				TestDataFactory.createTestUser(
						"Jane Smith", "jane.smith@example.com", "+1987654321");
		testUser3 =
				TestDataFactory.createTestUser(
						"Alice Johnson", "alice.johnson@example.com", "+1122334455");
	}

	@Nested
	@DisplayName("Basic CRUD Operations")
	class BasicCrudOperations {

		@Test
		@DisplayName("Should save and retrieve user successfully")
		void shouldSaveAndRetrieveUser() {
			// Given
			UserEntity savedUser = userRepository.save(testUser1);

			// When
			Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

			// Then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getName()).isEqualTo("John Doe");
			assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
			assertThat(foundUser.get().getPhone()).isEqualTo("+1234567890");
			assertThat(foundUser.get().getId()).isNotNull();
			assertThat(foundUser.get().getCreatedAt()).isNotNull();
			assertThat(foundUser.get().getUpdatedAt()).isNotNull();
		}

		@Test
		@DisplayName("Should delete user successfully")
		void shouldDeleteUserSuccessfully() {
			// Given
			UserEntity savedUser = userRepository.save(testUser1);
			UUID userId = savedUser.getId();

			// When
			userRepository.deleteById(userId);

			// Then
			Optional<UserEntity> deletedUser = userRepository.findById(userId);
			assertThat(deletedUser).isEmpty();
		}
	}

	@Nested
	@DisplayName("Email Operations")
	class EmailOperations {

		@Test
		@DisplayName("Should check if user exists by email")
		void shouldCheckIfUserExistsByEmail() {
			// Given
			userRepository.save(testUser1);

			// When & Then
			assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
			assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
		}

		@Test
		@DisplayName("Should find user by email")
		void shouldFindUserByEmail() {
			// Given
			userRepository.save(testUser1);

			// When
			UserEntity foundUser = userRepository.findByEmail("john.doe@example.com");

			// Then
			assertThat(foundUser).isNotNull();
			assertThat(foundUser.getName()).isEqualTo("John Doe");
			assertThat(foundUser.getEmail()).isEqualTo("john.doe@example.com");
		}

		@Test
		@DisplayName("Should return null when user not found by email")
		void shouldReturnNullWhenUserNotFoundByEmail() {
			// When
			UserEntity foundUser = userRepository.findByEmail("nonexistent@example.com");

			// Then
			assertThat(foundUser).isNull();
		}
	}

	@Nested
	@DisplayName("Search Operations")
	class SearchOperations {

		@BeforeEach
		void setUpSearchData() {
			userRepository.saveAll(List.of(testUser1, testUser2, testUser3));
		}

		@Test
		@DisplayName("Should search users by name")
		void shouldSearchUsersByName() {
			// Given
			UUID excludeUserId = testUser1.getId();

			// When
			List<UserEntity> foundUsers = userRepository.searchUsers("Jane", excludeUserId);

			// Then
			assertThat(foundUsers).hasSize(1);
			assertThat(foundUsers.get(0).getName()).isEqualTo("Jane Smith");
			assertThat(foundUsers.get(0).getId()).isNotEqualTo(excludeUserId);
		}

		@Test
		@DisplayName("Should search users by email")
		void shouldSearchUsersByEmail() {
			// Given
			UUID excludeUserId = testUser2.getId();

			// When
			List<UserEntity> foundUsers =
					userRepository.searchUsers("alice.johnson", excludeUserId);

			// Then
			assertThat(foundUsers).hasSize(1);
			assertThat(foundUsers.get(0).getEmail()).isEqualTo("alice.johnson@example.com");
			assertThat(foundUsers.get(0).getId()).isNotEqualTo(excludeUserId);
		}

		@Test
		@DisplayName("Should search users by phone")
		void shouldSearchUsersByPhone() {
			// Given
			UUID excludeUserId = testUser3.getId();

			// When
			List<UserEntity> foundUsers = userRepository.searchUsers("1234567890", excludeUserId);

			// Then
			assertThat(foundUsers).hasSize(1);
			assertThat(foundUsers.get(0).getPhone()).isEqualTo("+1234567890");
			assertThat(foundUsers.get(0).getId()).isNotEqualTo(excludeUserId);
		}

		@Test
		@DisplayName("Should perform case-insensitive search")
		void shouldPerformCaseInsensitiveSearch() {
			// Given
			UUID excludeUserId = testUser3.getId();

			// When
			List<UserEntity> foundUsers = userRepository.searchUsers("JOHN", excludeUserId);

			// Then
			assertThat(foundUsers).hasSize(1);
			assertThat(foundUsers.get(0).getName()).isEqualTo("John Doe");
		}

		@Test
		@DisplayName("Should exclude specified user from search results")
		void shouldExcludeSpecifiedUserFromSearchResults() {
			// Given
			UUID excludeUserId = testUser1.getId();

			// When
			List<UserEntity> foundUsers = userRepository.searchUsers("john", excludeUserId);

			// Then
			assertThat(foundUsers).hasSize(1);
			assertThat(foundUsers.get(0).getName()).isEqualTo("Alice Johnson");
		}

		@Test
		@DisplayName("Should return empty list when no users match search criteria")
		void shouldReturnEmptyListWhenNoUsersMatchSearchCriteria() {
			// Given
			UUID excludeUserId = UUID.randomUUID();

			// When
			List<UserEntity> foundUsers = userRepository.searchUsers("nonexistent", excludeUserId);

			// Then
			assertThat(foundUsers).isEmpty();
		}

		@Test
		@DisplayName("Should find users with partial name match")
		void shouldFindUsersWithPartialNameMatch() {
			// Given
			UUID excludeUserId = UUID.randomUUID();

			// When
			List<UserEntity> foundUsers = userRepository.searchUsers("Jo", excludeUserId);

			// Then
			assertThat(foundUsers).hasSize(2); // John and Johnson
			assertThat(foundUsers)
					.extracting(UserEntity::getName)
					.containsExactlyInAnyOrder("John Doe", "Alice Johnson");
		}
	}
}
