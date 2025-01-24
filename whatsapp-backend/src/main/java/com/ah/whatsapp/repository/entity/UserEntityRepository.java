package com.ah.whatsapp.repository.entity;

import com.ah.whatsapp.entity.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {
	boolean existsByEmail(String email);

	UserEntity findByEmail(String email);
}
