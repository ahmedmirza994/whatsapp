package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.User;

@Component
public class UserMapper {

	public UserEntity toEntity(User model) {
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(model, entity);
		return entity;
	}

	public User toModel(UserEntity entity) {
		User model = new User();
		BeanUtils.copyProperties(entity, model);
		return model;
	}

	public User toModel(UserSignupDto dto) {
		User model = new User();
		BeanUtils.copyProperties(dto, model);
		model.setCreatedAt(LocalDateTime.now());
		model.setUpdatedAt(LocalDateTime.now());
		return model;
	}

	public UserDto toDto(User model, String jwtToken) {
		return new UserDto(
			model.getId(),
			model.getName(),
			model.getEmail(),
			model.getPhone(),
			model.getProfilePictureUrl(),
			jwtToken
		);
	}

	public UserDto toDto(User model) {
		return toDto(model, null);
	}
}
