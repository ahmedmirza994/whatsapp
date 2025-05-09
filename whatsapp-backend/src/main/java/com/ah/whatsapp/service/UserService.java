package com.ah.whatsapp.service;

import com.ah.whatsapp.dto.UserUpdateDto;
import java.util.List;
import java.util.UUID;
import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
	User save(User user);

	UserDto registerUser(User user);

	UserDto loginUser(LoginDto loginDto);

	UserDto getUserById(UUID id);

	List<UserDto> searchUsers(String query, UUID excludeUserId);

	Boolean existsById(UUID id);

	UserDto updateUser(UUID userId, UserUpdateDto userUpdateDto);

	UserDto updateProfilePicture(UUID userId, MultipartFile profilePicture);
}
