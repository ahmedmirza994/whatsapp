/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ah.whatsapp.entity.ConversationParticipantEntity;

@Repository
public interface ConversationParticipantEntityRepository
		extends JpaRepository<ConversationParticipantEntity, UUID> {

	@Query(
			"""
			select cpe from ConversationParticipantEntity cpe
			join fetch cpe.user
			where cpe.conversation.id = :conversationId
			""")
	List<ConversationParticipantEntity> findByConversationIdAndIsActiveTrueWithUser(
			@Param("conversationId") UUID conversationId);

	@Query(
			"""
			select cpe from ConversationParticipantEntity cpe
			join fetch cpe.user
			where cpe.conversation.id = :conversationId
			and cpe.user.id = :userId
			and cpe.isActive = true
			""")
	Optional<ConversationParticipantEntity> findByConversationIdAndUserIdAndIsActiveTrue(
			@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

	boolean existsByConversationIdAndUserIdAndIsActiveTrue(
			@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

	@Query(
			"""
			select cpe from ConversationParticipantEntity cpe
			join fetch cpe.user
			where cpe.conversation.id in :conversationIds
			""")
	List<ConversationParticipantEntity> findByConversationIdInAndIsActiveTrueWithUser(
			@Param("conversationIds") List<UUID> conversationIds);

	@Query(
			"""
			select cpe from ConversationParticipantEntity cpe
			join fetch cpe.user
			where cpe.conversation.id = :conversationId
			""")
	List<ConversationParticipantEntity> findByConversationIdWithUser(
			@Param("conversationId") UUID conversationId);

	@Query(
			"""
			select cpe from ConversationParticipantEntity cpe
			join fetch cpe.user
			where cpe.conversation.id = :conversationId
			and cpe.user.id = :userId
			""")
	Optional<ConversationParticipantEntity> findByConversationIdAndUserId(
			@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
