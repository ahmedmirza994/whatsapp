package com.ah.whatsapp.repository;

import com.ah.whatsapp.model.User;
import java.util.Optional;

public interface UserRepository {
	User save(User user);

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);
}
