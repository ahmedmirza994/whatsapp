package com.ah.whatsapp.service.impl;

import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
		return new JwtUser(
			user.getEmail(),
			user.getId(),
			user.getPassword()
		);
	}
}
