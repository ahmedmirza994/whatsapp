package com.ah.whatsapp.service.impl;

import com.ah.whatsapp.dto.LoginDto;
import com.ah.whatsapp.dto.UserDto;
import com.ah.whatsapp.exception.InvalidCredentialsException;
import com.ah.whatsapp.exception.UserAlreadyExistsException;
import com.ah.whatsapp.mapper.UserMapper;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.UserService;
import com.ah.whatsapp.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

	public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.jwtUtil = jwtUtil;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
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
		User registeredUser = userRepository.save(user);
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
}
