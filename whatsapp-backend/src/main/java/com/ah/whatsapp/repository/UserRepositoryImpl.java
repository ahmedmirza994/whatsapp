package com.ah.whatsapp.repository;

import org.springframework.stereotype.Component;

import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
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

}
