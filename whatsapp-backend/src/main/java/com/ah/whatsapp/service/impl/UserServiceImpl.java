package com.ah.whatsapp.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.dto.UserUpdateDto;
import com.ah.whatsapp.enums.FolderName;
import com.ah.whatsapp.exception.InvalidCredentialsException;
import com.ah.whatsapp.exception.UserAlreadyExistsException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.FileStorage;
import com.ah.whatsapp.service.UserService;
import com.ah.whatsapp.util.JwtUtil;

@Transactional
@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final FileStorage fileStorage;

	public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, FileStorage fileStorage) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.jwtUtil = jwtUtil;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.fileStorage = fileStorage;
	}

	@Override
	public User save(User user) {
		return userRepository.save(user);
	}

	@Override
	public UserDto registerUser(User user) {
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists.");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User registeredUser = save(user);
		String jwtToken = jwtUtil.generateToken(registeredUser.getEmail());
		return userMapper.toDto(registeredUser, jwtToken);
	}

	@Override
	public UserDto loginUser(LoginDto loginDto) {
		User user = userRepository.findByEmail(loginDto.email())
			.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginDto.email()));

		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
			);

		} catch (AuthenticationException e) {
			throw new InvalidCredentialsException("Invalid credentials");
		}

		return userMapper.toDto(user, jwtUtil.generateToken(loginDto.email()));
	}

	@Override
	public UserDto getUserById(UUID id) {
		return userRepository.findById(id)
			.map(userMapper::toDto)
			.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
	}

	@Override
    public List<UserDto> searchUsers(String query, UUID excludeUserId) {
        if (ObjectUtils.isEmpty(query) ) {
            return List.of();
        }

        List<User> users = userRepository.searchUsers(query.trim(), excludeUserId);

        return users.stream()
			.map(userMapper::toDto)
			.toList();
    }

    @Override
    public Boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

	@Override
	public UserDto updateUser(UUID userId, UserUpdateDto userUpdateDto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

		boolean updated = false;

		if (userUpdateDto.name() != null && !userUpdateDto.name().equals(user.getName())) {
			user.setName(userUpdateDto.name());
			updated = true;
		}

		if (userUpdateDto.phone() != null && !userUpdateDto.phone().equals(user.getPhone())) {
			user.setPhone(userUpdateDto.phone());
			updated = true;
		}

		if (updated) {
			user.setUpdatedAt(LocalDateTime.now());
			User savedUser = userRepository.save(user);
			return userMapper.toDto(savedUser);
		} else {
			return userMapper.toDto(user);
		}
	}

	@Override
	public UserDto updateProfilePicture(UUID userId, MultipartFile profilePicture) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
		try {
			String filename = fileStorage.storeFile(profilePicture, FolderName.PROFILE_PICTURES, userId.toString());
			user.setProfilePicture(filename);
			user.setUpdatedAt(LocalDateTime.now());
			User savedUser = userRepository.save(user);
			return userMapper.toDto(savedUser);
		} catch (IOException ex) {
			throw new RuntimeException("Could not store profile picture for user " + userId + ". Please try again!", ex);
		}
	}
}
