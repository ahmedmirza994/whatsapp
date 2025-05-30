/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.entity.UserEntityRepository;
import com.ah.whatsapp.repository.impl.UserRepositoryImpl;

/**
 * Comprehensive Unit Tests for UserRepository
 * Following industry best practices for repository layer testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Mock private UserEntityRepository userEntityRepository;

    @Mock private UserMapper userMapper;

    @InjectMocks private UserRepositoryImpl userRepository;

    private User testUser;
    private UserEntity testUserEntity;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // Using UserTestDataBuilder for consistent test data
        testUser =
                aUser().withId(testUserId)
                        .withName("John Doe")
                        .withEmail("john.doe@example.com")
                        .withPassword("password123")
                        .withPhone("+1234567890")
                        .withProfilePicture("http://example.com/profile.jpg")
                        .build();

        testUserEntity =
                aUser().withId(testUserId)
                        .withName("John Doe")
                        .withEmail("john.doe@example.com")
                        .withPassword("password123")
                        .withPhone("+1234567890")
                        .withProfilePicture("http://example.com/profile.jpg")
                        .buildEntity();
    }

    @Nested
    @DisplayName("Save User Tests")
    class SaveUserTests {

        @Test
        @DisplayName("Should save user successfully and return mapped model")
        void save_ShouldReturnSavedUser_WhenValidUserProvided() {
            // Given
            when(userMapper.toEntity(testUser)).thenReturn(testUserEntity);
            when(userEntityRepository.save(testUserEntity)).thenReturn(testUserEntity);
            when(userMapper.toModel(testUserEntity)).thenReturn(testUser);

            // When
            User savedUser = userRepository.save(testUser);

            // Then
            assertNotNull(savedUser);
            assertEquals(testUser.getId(), savedUser.getId());
            assertEquals(testUser.getName(), savedUser.getName());
            assertEquals(testUser.getEmail(), savedUser.getEmail());
            assertEquals(testUser.getPassword(), savedUser.getPassword());
            assertEquals(testUser.getPhone(), savedUser.getPhone());
            assertEquals(testUser.getProfilePicture(), savedUser.getProfilePicture());

            // Verify interactions
            verify(userMapper).toEntity(testUser);
            verify(userEntityRepository).save(testUserEntity);
            verify(userMapper).toModel(testUserEntity);
        }

        @Test
        @DisplayName("Should handle user with minimal required fields")
        void save_ShouldHandleMinimalUser_WhenOnlyRequiredFieldsProvided() {
            // Given
            User minimalUser =
                    aUser().withName("Jane")
                            .withEmail("jane@example.com")
                            .withPassword("password")
                            .withPhone(null)
                            .withProfilePicture(null)
                            .build();

            UserEntity minimalEntity =
                    aUser().withName("Jane")
                            .withEmail("jane@example.com")
                            .withPassword("password")
                            .withPhone(null)
                            .withProfilePicture(null)
                            .buildEntity();

            when(userMapper.toEntity(minimalUser)).thenReturn(minimalEntity);
            when(userEntityRepository.save(minimalEntity)).thenReturn(minimalEntity);
            when(userMapper.toModel(minimalEntity)).thenReturn(minimalUser);

            // When
            User savedUser = userRepository.save(minimalUser);

            // Then
            assertNotNull(savedUser);
            assertEquals("Jane", savedUser.getName());
            assertEquals("jane@example.com", savedUser.getEmail());
            verify(userEntityRepository).save(minimalEntity);
        }
    }

    @Nested
    @DisplayName("Exists By Email Tests")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Should return true when user exists with given email")
        void existsByEmail_ShouldReturnTrue_WhenUserExistsWithEmail() {
            // Given
            String email = "existing@example.com";
            when(userEntityRepository.existsByEmail(email)).thenReturn(true);

            // When
            boolean exists = userRepository.existsByEmail(email);

            // Then
            assertTrue(exists);
            verify(userEntityRepository).existsByEmail(email);
        }

        @Test
        @DisplayName("Should return false when no user exists with given email")
        void existsByEmail_ShouldReturnFalse_WhenNoUserExistsWithEmail() {
            // Given
            String email = "nonexistent@example.com";
            when(userEntityRepository.existsByEmail(email)).thenReturn(false);

            // When
            boolean exists = userRepository.existsByEmail(email);

            // Then
            assertFalse(exists);
            verify(userEntityRepository).existsByEmail(email);
        }

        @Test
        @DisplayName("Should handle empty email string")
        void existsByEmail_ShouldHandleEmptyEmail() {
            // Given
            String email = "";
            when(userEntityRepository.existsByEmail(email)).thenReturn(false);

            // When
            boolean exists = userRepository.existsByEmail(email);

            // Then
            assertFalse(exists);
            verify(userEntityRepository).existsByEmail(email);
        }
    }

    @Nested
    @DisplayName("Find By Email Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should return user when found by email")
        void findByEmail_ShouldReturnUser_WhenUserExistsWithEmail() {
            // Given
            String email = "john.doe@example.com";
            when(userEntityRepository.findByEmail(email)).thenReturn(testUserEntity);
            when(userMapper.toModel(testUserEntity)).thenReturn(testUser);

            // When
            Optional<User> foundUser = userRepository.findByEmail(email);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(testUser.getId(), foundUser.get().getId());
            assertEquals(email, foundUser.get().getEmail());
            verify(userEntityRepository).findByEmail(email);
            verify(userMapper).toModel(testUserEntity);
        }

        @Test
        @DisplayName("Should return empty optional when no user found by email")
        void findByEmail_ShouldReturnEmpty_WhenNoUserExistsWithEmail() {
            // Given
            String email = "nonexistent@example.com";
            when(userEntityRepository.findByEmail(email)).thenReturn(null);

            // When
            Optional<User> foundUser = userRepository.findByEmail(email);

            // Then
            assertFalse(foundUser.isPresent());
            verify(userEntityRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Should handle null email gracefully")
        void findByEmail_ShouldReturnEmpty_WhenEmailIsNull() {
            // Given
            when(userEntityRepository.findByEmail(null)).thenReturn(null);

            // When
            Optional<User> foundUser = userRepository.findByEmail(null);

            // Then
            assertFalse(foundUser.isPresent());
            verify(userEntityRepository).findByEmail(null);
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return user when found by ID")
        void findById_ShouldReturnUser_WhenUserExistsWithId() {
            // Given
            when(userEntityRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));
            when(userMapper.toModel(testUserEntity)).thenReturn(testUser);

            // When
            Optional<User> foundUser = userRepository.findById(testUserId);

            // Then
            assertTrue(foundUser.isPresent());
            assertEquals(testUserId, foundUser.get().getId());
            assertEquals(testUser.getName(), foundUser.get().getName());
            verify(userEntityRepository).findById(testUserId);
            verify(userMapper).toModel(testUserEntity);
        }

        @Test
        @DisplayName("Should return empty optional when no user found by ID")
        void findById_ShouldReturnEmpty_WhenNoUserExistsWithId() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(userEntityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When
            Optional<User> foundUser = userRepository.findById(nonExistentId);

            // Then
            assertFalse(foundUser.isPresent());
            verify(userEntityRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should return matching users when search query provided")
        void searchUsers_ShouldReturnMatchingUsers_WhenValidQueryProvided() {
            // Given
            String query = "John";
            UUID excludeUserId = UUID.randomUUID();
            List<UserEntity> entityResults = Arrays.asList(testUserEntity);

            when(userEntityRepository.searchUsers(query, excludeUserId)).thenReturn(entityResults);
            when(userMapper.toModel(testUserEntity)).thenReturn(testUser);

            // When
            List<User> results = userRepository.searchUsers(query, excludeUserId);

            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(testUser.getId(), results.get(0).getId());
            assertEquals(testUser.getName(), results.get(0).getName());
            verify(userEntityRepository).searchUsers(query, excludeUserId);
            verify(userMapper).toModel(testUserEntity);
        }

        @Test
        @DisplayName("Should return empty list when no users match search criteria")
        void searchUsers_ShouldReturnEmptyList_WhenNoUsersMatch() {
            // Given
            String query = "NonExistent";
            UUID excludeUserId = UUID.randomUUID();
            when(userEntityRepository.searchUsers(query, excludeUserId))
                    .thenReturn(Collections.emptyList());

            // When
            List<User> results = userRepository.searchUsers(query, excludeUserId);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(userEntityRepository).searchUsers(query, excludeUserId);
        }

        @Test
        @DisplayName("Should handle multiple search results correctly")
        void searchUsers_ShouldReturnMultipleUsers_WhenMultipleUsersMatch() {
            // Given
            String query = "test";
            UUID excludeUserId = UUID.randomUUID();

            User secondUser =
                    aUser().withName("Test User 2").withEmail("test2@example.com").build();

            UserEntity secondEntity =
                    aUser().withName("Test User 2").withEmail("test2@example.com").buildEntity();

            List<UserEntity> entityResults = Arrays.asList(testUserEntity, secondEntity);

            when(userEntityRepository.searchUsers(query, excludeUserId)).thenReturn(entityResults);
            when(userMapper.toModel(testUserEntity)).thenReturn(testUser);
            when(userMapper.toModel(secondEntity)).thenReturn(secondUser);

            // When
            List<User> results = userRepository.searchUsers(query, excludeUserId);

            // Then
            assertNotNull(results);
            assertEquals(2, results.size());
            verify(userEntityRepository).searchUsers(query, excludeUserId);
        }

        @Test
        @DisplayName("Should handle empty query string")
        void searchUsers_ShouldHandleEmptyQuery() {
            // Given
            String query = "";
            UUID excludeUserId = UUID.randomUUID();
            when(userEntityRepository.searchUsers(query, excludeUserId))
                    .thenReturn(Collections.emptyList());

            // When
            List<User> results = userRepository.searchUsers(query, excludeUserId);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(userEntityRepository).searchUsers(query, excludeUserId);
        }
    }

    @Nested
    @DisplayName("Exists By ID Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true when user exists with given ID")
        void existsById_ShouldReturnTrue_WhenUserExistsWithId() {
            // Given
            when(userEntityRepository.existsById(testUserId)).thenReturn(true);

            // When
            boolean exists = userRepository.existsById(testUserId);

            // Then
            assertTrue(exists);
            verify(userEntityRepository).existsById(testUserId);
        }

        @Test
        @DisplayName("Should return false when no user exists with given ID")
        void existsById_ShouldReturnFalse_WhenNoUserExistsWithId() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(userEntityRepository.existsById(nonExistentId)).thenReturn(false);

            // When
            boolean exists = userRepository.existsById(nonExistentId);

            // Then
            assertFalse(exists);
            verify(userEntityRepository).existsById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should return correct count of users")
        void count_ShouldReturnCorrectCount_WhenUsersExist() {
            // Given
            long expectedCount = 5L;
            when(userEntityRepository.count()).thenReturn(expectedCount);

            // When
            long actualCount = userRepository.count();

            // Then
            assertEquals(expectedCount, actualCount);
            verify(userEntityRepository).count();
        }

        @Test
        @DisplayName("Should return zero when no users exist")
        void count_ShouldReturnZero_WhenNoUsersExist() {
            // Given
            when(userEntityRepository.count()).thenReturn(0L);

            // When
            long actualCount = userRepository.count();

            // Then
            assertEquals(0L, actualCount);
            verify(userEntityRepository).count();
        }
    }
}
