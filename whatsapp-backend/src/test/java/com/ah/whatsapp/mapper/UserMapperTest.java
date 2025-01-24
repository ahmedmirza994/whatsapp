package com.ah.whatsapp.mapper;

import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

	private UserMapper userMapper;

	@BeforeEach
	public void setUp() {
		userMapper = new UserMapper();
	}

	@Test
	public void testToModelFromEntity() {
		UserEntity userEntity = new UserEntity();
		userEntity.setName("John Doe");
		userEntity.setEmail("john.doe@example.com");
		userEntity.setPassword("password123");
		userEntity.setPhone("+1234567890");
		userEntity.setProfilePictureUrl("http://example.com/profile.jpg");

		User user = userMapper.toModel(userEntity);

		assertEquals(userEntity.getName(), user.getName());
		assertEquals(userEntity.getEmail(), user.getEmail());
		assertEquals(userEntity.getPassword(), user.getPassword());
		assertEquals(userEntity.getPhone(), user.getPhone());
		assertEquals(userEntity.getProfilePictureUrl(), user.getProfilePictureUrl());
	}

	@Test
	public void testToModelFromDto() {
		UserSignupDto userSignupDto = new UserSignupDto(
			"John Doe",
			"john.doe@example.com",
			"password123",
			"+1234567890"
		);

		User user = userMapper.toModel(userSignupDto);

		assertEquals(userSignupDto.name(), user.getName());
		assertEquals(userSignupDto.email(), user.getEmail());
		assertEquals(userSignupDto.password(), user.getPassword());
		assertEquals(userSignupDto.phone(), user.getPhone());
	}

	@Test
	public void testToEntity() {
		User user = new User();
		user.setName("John Doe");
		user.setEmail("john.doe@example.com");
		user.setPassword("password123");
		user.setPhone("+1234567890");
		user.setProfilePictureUrl("http://example.com/profile.jpg");

		UserEntity userEntity = userMapper.toEntity(user);

		assertEquals(user.getName(), userEntity.getName());
		assertEquals(user.getEmail(), userEntity.getEmail());
		assertEquals(user.getPassword(), userEntity.getPassword());
		assertEquals(user.getPhone(), userEntity.getPhone());
		assertEquals(user.getProfilePictureUrl(), userEntity.getProfilePictureUrl());
	}

}
