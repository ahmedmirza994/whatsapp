/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.User;

public class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    public void testToModelFromEntity() {
        UserEntity userEntity =
                aUser().withName("John Doe")
                        .withEmail("john.doe@example.com")
                        .withPassword("password123")
                        .withPhone("+1234567890")
                        .withProfilePicture("http://example.com/profile.jpg")
                        .buildEntity();

        User user = userMapper.toModel(userEntity);

        assertEquals(userEntity.getId(), user.getId());
        assertEquals(userEntity.getName(), user.getName());
        assertEquals(userEntity.getEmail(), user.getEmail());
        assertEquals(userEntity.getPassword(), user.getPassword());
        assertEquals(userEntity.getPhone(), user.getPhone());
        assertEquals(userEntity.getProfilePicture(), user.getProfilePicture());
        assertEquals(userEntity.getCreatedAt(), user.getCreatedAt());
        assertEquals(userEntity.getUpdatedAt(), user.getUpdatedAt());
    }

    @Test
    public void testToModelFromDto() {
        UserSignupDto userSignupDto =
                new UserSignupDto("John Doe", "john.doe@example.com", "password123", "+1234567890");

        User user = userMapper.toModel(userSignupDto);

        assertEquals(userSignupDto.name(), user.getName());
        assertEquals(userSignupDto.email(), user.getEmail());
        assertEquals(userSignupDto.password(), user.getPassword());
        assertEquals(userSignupDto.phone(), user.getPhone());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    public void testToEntity() {
        User user =
                aUser().withName("John Doe")
                        .withEmail("john.doe@example.com")
                        .withPassword("password123")
                        .withPhone("+1234567890")
                        .withProfilePicture("http://example.com/profile.jpg")
                        .build();

        UserEntity userEntity = userMapper.toEntity(user);

        assertEquals(user.getId(), userEntity.getId());
        assertEquals(user.getName(), userEntity.getName());
        assertEquals(user.getEmail(), userEntity.getEmail());
        assertEquals(user.getPassword(), userEntity.getPassword());
        assertEquals(user.getPhone(), userEntity.getPhone());
        assertEquals(user.getProfilePicture(), userEntity.getProfilePicture());
        assertEquals(user.getCreatedAt(), userEntity.getCreatedAt());
        assertEquals(user.getUpdatedAt(), userEntity.getUpdatedAt());
    }

    @Test
    public void testToDtoWithToken() {
        User user = aUser().build();
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        UserDto result = userMapper.toDto(user, jwtToken);

        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getPhone(), result.phone());
        assertEquals(user.getProfilePicture(), result.profilePicture());
        assertEquals(jwtToken, result.jwtToken());
    }

    @Test
    public void testToDtoWithoutToken() {
        User user = aUser().build();

        UserDto result = userMapper.toDto(user);

        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getPhone(), result.phone());
        assertEquals(user.getProfilePicture(), result.profilePicture());
        assertNull(result.jwtToken());
    }

    @Test
    public void testToDtoWithNullToken() {
        User user = aUser().build();

        UserDto result = userMapper.toDto(user, null);

        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getPhone(), result.phone());
        assertEquals(user.getProfilePicture(), result.profilePicture());
        assertNull(result.jwtToken());
    }

    @Test
    public void testToModelFromEntity_WithNullValues() {
        UserEntity userEntity =
                aUser().withNullValues().withEmail("test@example.com").buildEntity();

        User user = userMapper.toModel(userEntity);

        assertEquals(userEntity.getId(), user.getId());
        assertNull(user.getName());
        assertEquals(userEntity.getEmail(), user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getPhone());
        assertNull(user.getProfilePicture());
    }

    @Test
    public void testToEntity_WithNullValues() {
        User user = aUser().withNullValues().withEmail("test@example.com").build();

        UserEntity userEntity = userMapper.toEntity(user);

        assertEquals(user.getId(), userEntity.getId());
        assertNull(userEntity.getName());
        assertEquals(user.getEmail(), userEntity.getEmail());
        assertNull(userEntity.getPassword());
        assertNull(userEntity.getPhone());
        assertNull(userEntity.getProfilePicture());
    }

    @Test
    public void testRoundTripConversion() {
        User originalUser =
                aUser().withName("John Doe")
                        .withEmail("john.doe@example.com")
                        .withPassword("hashedPassword123")
                        .withPhone("+1234567890")
                        .withProfilePicture("http://example.com/profile.jpg")
                        .build();

        UserEntity entity = userMapper.toEntity(originalUser);
        User resultUser = userMapper.toModel(entity);

        assertEquals(originalUser.getId(), resultUser.getId());
        assertEquals(originalUser.getName(), resultUser.getName());
        assertEquals(originalUser.getEmail(), resultUser.getEmail());
        assertEquals(originalUser.getPassword(), resultUser.getPassword());
        assertEquals(originalUser.getPhone(), resultUser.getPhone());
        assertEquals(originalUser.getProfilePicture(), resultUser.getProfilePicture());
        assertEquals(originalUser.getCreatedAt(), resultUser.getCreatedAt());
        assertEquals(originalUser.getUpdatedAt(), resultUser.getUpdatedAt());
    }

    @Test
    public void testToModelFromDto_SetsTimestamps() {
        UserSignupDto userSignupDto =
                new UserSignupDto("Jane Smith", "jane@example.com", "securepass", "+0987654321");

        LocalDateTime beforeConversion = LocalDateTime.now().minusSeconds(1);
        User user = userMapper.toModel(userSignupDto);
        LocalDateTime afterConversion = LocalDateTime.now().plusSeconds(1);

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getCreatedAt().isAfter(beforeConversion));
        assertTrue(user.getCreatedAt().isBefore(afterConversion));
        assertTrue(user.getUpdatedAt().isAfter(beforeConversion));
        assertTrue(user.getUpdatedAt().isBefore(afterConversion));
        assertTrue(
                Math.abs(user.getCreatedAt().getNano() - user.getUpdatedAt().getNano()) < 1000000);
    }
}
