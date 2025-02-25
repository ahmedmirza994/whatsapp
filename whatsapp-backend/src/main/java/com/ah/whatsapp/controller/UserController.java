package com.ah.whatsapp.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.service.UserService;

import jakarta.validation.Valid;

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

	@GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchUsers(
            @RequestParam(required = false) String query) {

        List<UserDto> results = userService.searchUsers(query);
        ApiResponse<List<UserDto>> response = ApiResponse.success(results);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        UserDto userDto = userService.getUserById(id);
        ApiResponse<UserDto> response = ApiResponse.success(userDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
