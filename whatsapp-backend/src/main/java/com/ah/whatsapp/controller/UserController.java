package com.ah.whatsapp.controller;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;

	public UserController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<UserDto>> signup(@Valid @RequestBody UserSignupDto userSignupDto) {
		User user = userMapper.toModel(userSignupDto);
		UserDto userDto = userService.registerUser(user);
		ApiResponse<UserDto> response = ApiResponse.success(userDto);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginDto loginDto) {
		UserDto userDto = userService.loginUser(loginDto);
		ApiResponse<UserDto> response = ApiResponse.success(userDto);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
