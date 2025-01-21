package com.ah.whatsapp.repository.entity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ah.whatsapp.entity.UserEntity;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {

}
