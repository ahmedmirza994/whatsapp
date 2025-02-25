package com.ah.whatsapp.service;

import java.util.List;
import java.util.UUID;

import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.model.User;

public interface UserService {
	User save(User user);

	UserDto registerUser(User user);

	UserDto loginUser(LoginDto loginDto);

	UserDto getUserById(UUID id);

	List<UserDto> searchUsers(String query);
}
