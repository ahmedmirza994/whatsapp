/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ah.whatsapp.entity.MessageEntity;

@Repository
public interface MessageEntityRepository extends JpaRepository<MessageEntity, UUID> {

	@Query(
			"""
			select m from MessageEntity m
			join fetch m.sender
			where m.conversation.id = :conversationId
			and m.sentAt > :sentAt
			""")
	List<MessageEntity> findByConversationIdAndSendAtAfterOrderBySentAtAsc(
			@Param("conversationId") UUID conversationId, @Param("sentAt") LocalDateTime sentAt);

	List<MessageEntity> findBySenderId(UUID senderId);

	@Query(
			"""
			select m from MessageEntity m
			join fetch m.sender
			where m.conversation.id = :conversationId
			order by m.sentAt desc
			limit 1
			""")
	Optional<MessageEntity> findByConversationIdOrderBySentAtDesc(
			@Param("conversationId") UUID conversationId);

	/**
	 * Finds the IDs of the latest message entity for each conversation ID in the provided list
	 * using a window function to rank messages within each conversation partition.
	 *
	 * @param conversationIds The list of conversation IDs.
	 * @return A list of the UUIDs for the latest messages.
	 */
	@Query(
			value =
"""
		select m.id from ( select me.id, row_number() over(partition by me.conversation_id order by me.sent_at desc) as rn
		from messages me
		where me.conversation_id in (:conversationIds)
		) m
		where m.rn = 1
""",
			nativeQuery = true)
	List<UUID> findLatestMessageIdsForConversationIds(
			@Param("conversationIds") List<UUID> conversationIds);

	/**
	 * Finds MessageEntities by their IDs, eagerly fetching the associated sender (UserEntity).
	 *
	 * @param messageIds The list of MessageEntity IDs to fetch.
	 * @return A list of MessageEntity objects with their sender initialized.
	 */
	@Query("select m from MessageEntity m join fetch m.sender where m.id in :messageIds")
	List<MessageEntity> findMessagesByIdsWithSender(@Param("messageIds") List<UUID> messageIds);
}
