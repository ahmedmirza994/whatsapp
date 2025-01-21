package com.ah.whatsapp.service.impl;

import org.springframework.stereotype.Service;

import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public User save(User user) {
		return userRepository.save(user);
	}

}
