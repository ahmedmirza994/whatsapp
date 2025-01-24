package com.ah.whatsapp.service;

import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.model.User;

public interface UserService {
	User save(User user);

	UserDto registerUser(User user);

	UserDto loginUser(LoginDto loginDto);
}
