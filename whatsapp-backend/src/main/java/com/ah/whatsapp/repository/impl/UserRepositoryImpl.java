/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.repository.entity.UserEntityRepository;

@Component
public class UserRepositoryImpl implements UserRepository {

	private final UserEntityRepository userEntityRepository;
	private final UserMapper userMapper;

	public UserRepositoryImpl(UserEntityRepository userEntityRepository, UserMapper userMapper) {
		this.userEntityRepository = userEntityRepository;
		this.userMapper = userMapper;
	}

	@Override
	public User save(User user) {
		UserEntity userEntity = userEntityRepository.save(userMapper.toEntity(user));
		return userMapper.toModel(userEntity);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userEntityRepository.existsByEmail(email);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		UserEntity userEntity = userEntityRepository.findByEmail(email);
		if (userEntity == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(userMapper.toModel(userEntity));
	}

	@Override
	public Optional<User> findById(UUID id) {
		return userEntityRepository.findById(id).map(userMapper::toModel);
	}

	@Override
	public List<User> searchUsers(String query, UUID excludeUserId) {
		return userEntityRepository.searchUsers(query, excludeUserId).stream()
				.map(userMapper::toModel)
				.toList();
	}

	@Override
	public boolean existsById(UUID id) {
		return userEntityRepository.existsById(id);
	}

	@Override
	public long count() {
		return userEntityRepository.count();
	}
}
