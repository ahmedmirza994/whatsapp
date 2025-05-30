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

import com.ah.whatsapp.entity.ConversationEntity;

@Repository
public interface ConversationEntityRepository extends JpaRepository<ConversationEntity, UUID> {
	@Query(
			value =
					"""
					select c.* from conversations c
					join conversation_participants cp on c.id = cp.conversation_id
					where cp.user_id = :userId and cp.is_active = true
					order by c.updated_at desc
					""",
			nativeQuery = true)
	List<ConversationEntity> findConversationsByUserId(@Param("userId") UUID userId);

	@Query(
			value =
					"""
					select c.id from conversations c
					join conversation_participants cp on c.id = cp.conversation_id
					where cp.user_id in :userIds
					group by c.id
					having count(distinct cp.user_id) = :participantCount
					and count(cp.id) = :participantCount
					""",
			nativeQuery = true)
	List<UUID> findConversationsWithParticipants(
			@Param("userIds") List<UUID> userIds, @Param("participantCount") Long participantCount);
}
