/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository.entity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ah.whatsapp.entity.UserEntity;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);

    UserEntity findByEmail(String email);

    @Query(
            value =
                    "select * from users where (name ilike '%' || :query || '%' or email ilike '%'"
                            + " || :query || '%' or phone like '%' || :query || '%') and id !="
                            + " :excludeUserId",
            nativeQuery = true)
    List<UserEntity> searchUsers(
            @Param("query") String query, @Param("excludeUserId") UUID excludeUserId);
}
