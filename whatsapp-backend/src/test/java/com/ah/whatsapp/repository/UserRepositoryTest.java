package com.ah.whatsapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.entity.UserEntityRepository;

public class UserRepositoryTest {

	@Mock
	private UserEntityRepository userEntityRepository;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserRepositoryImpl userRepositoryImpl;

	private User user;
	private UserEntity userEntity;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		userEntity = new UserEntity();
		userEntity.setName("John Doe");
		userEntity.setEmail("john.doe@example.com");
		userEntity.setPassword("password123");
		userEntity.setPhone("+1234567890");
		userEntity.setProfilePictureUrl("http://example.com/profile.jpg");

		user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        user.setPhone("+1234567890");
        user.setProfilePictureUrl("http://example.com/profile.jpg");

		when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);
		when(userMapper.toModel(any(UserEntity.class))).thenReturn(user);
	}

	@Test
	public void testSave() {
		when(userEntityRepository.save(any(UserEntity.class))).thenReturn(userEntity);

		User savedUser = userRepositoryImpl.save(user);

		assertEquals(user.getName(), savedUser.getName());
		assertEquals(user.getEmail(), savedUser.getEmail());
		assertEquals(user.getPassword(), savedUser.getPassword());
		assertEquals(user.getPhone(), savedUser.getPhone());
		assertEquals(user.getProfilePictureUrl(), savedUser.getProfilePictureUrl());
	}
}
