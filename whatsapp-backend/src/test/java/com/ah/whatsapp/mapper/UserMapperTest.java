package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setName("John Doe");
        userEntity.setEmail("john.doe@example.com");
        userEntity.setPassword("password123");
        userEntity.setPhone("+1234567890");
        userEntity.setProfilePicture("http://example.com/profile.jpg");
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());

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
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        user.setPhone("+1234567890");
        user.setProfilePicture("http://example.com/profile.jpg");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

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
        User user = createTestUser();
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
        User user = createTestUser();

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
        User user = createTestUser();

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
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setName(null);
        userEntity.setEmail("test@example.com");
        userEntity.setPassword(null);
        userEntity.setPhone(null);
        userEntity.setProfilePicture(null);

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
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(null);
        user.setEmail("test@example.com");
        user.setPassword(null);
        user.setPhone(null);
        user.setProfilePicture(null);

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
        User originalUser = createTestUser();

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
        assertTrue(Math.abs(user.getCreatedAt().getNano() - user.getUpdatedAt().getNano()) < 1000000);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedPassword123");
        user.setPhone("+1234567890");
        user.setProfilePicture("http://example.com/profile.jpg");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
