package com.ah.whatsapp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ah.whatsapp.model.User;

public interface UserRepository {
	User save(User user);

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	Optional<User> findById(UUID id);

	List<User> searchUsers(String query);
}
