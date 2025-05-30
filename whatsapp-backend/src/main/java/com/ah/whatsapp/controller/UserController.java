/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.dto.UserUpdateDto;
import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.service.FileStorage;
import com.ah.whatsapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;
	private final FileStorage fileStorage;

	public UserController(UserService userService, UserMapper userMapper, FileStorage fileStorage) {
		this.userService = userService;
		this.userMapper = userMapper;
		this.fileStorage = fileStorage;
	}

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<UserDto>> signup(
			@Valid @RequestBody UserSignupDto userSignupDto) {
		User user = userMapper.toModel(userSignupDto);
		UserDto userDto = userService.registerUser(user);
		ApiResponse<UserDto> response = ApiResponse.success(userDto);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginDto loginDto) {
		UserDto userDto = userService.loginUser(loginDto);
		ApiResponse<UserDto> response = ApiResponse.success(userDto);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<UserDto>>> searchUsers(
			@RequestParam(required = true, name = "query") String query,
			@AuthenticationPrincipal JwtUser currentUser) {

		List<UserDto> results = userService.searchUsers(query, currentUser.getUserId());
		ApiResponse<List<UserDto>> response = ApiResponse.success(results);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
		UserDto userDto = userService.getUserById(id);
		ApiResponse<UserDto> response = ApiResponse.success(userDto);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/me")
	public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(
			@AuthenticationPrincipal JwtUser currentUser,
			@Valid @RequestBody UserUpdateDto userUpdateDto) {
		UserDto updatedUser = userService.updateUser(currentUser.getUserId(), userUpdateDto);
		ApiResponse<UserDto> response = ApiResponse.success(updatedUser);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/me/picture")
	public ResponseEntity<ApiResponse<UserDto>> uploadProfilePicture(
			@AuthenticationPrincipal JwtUser currentUser,
			@RequestParam("file") MultipartFile file) {

		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body(ApiResponse.badRequest("File cannot be empty"));
		}

		if (file.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.badRequest("File size exceeds limit"));
		}

		UserDto updatedUser = userService.updateProfilePicture(currentUser.getUserId(), file);
		ApiResponse<UserDto> response = ApiResponse.success(updatedUser);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/me/picture")
	public ResponseEntity<Resource> getCurrentUserProfilePicture(
			@AuthenticationPrincipal JwtUser currentUser, HttpServletRequest request) {
		if (currentUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			UserDto userDto = userService.getUserById(currentUser.getUserId());
			String filename = userDto.profilePicture();

			if (filename == null || filename.isBlank()) {
				return ResponseEntity.notFound().build();
			}

			Resource resource =
					fileStorage.loadFileAsResource(FolderName.PROFILE_PICTURES, filename);

			// 3. Determine content type
			String contentType = null;
			try {
				contentType =
						request.getServletContext()
								.getMimeType(resource.getFile().getAbsolutePath());
			} catch (IOException ex) {
			}
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					// .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
					// resource.getFilename() + "\"") // Optional
					.body(resource);

		} catch (MalformedURLException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (RuntimeException ex) {
			return ResponseEntity.notFound().build();
		}
	}
}
