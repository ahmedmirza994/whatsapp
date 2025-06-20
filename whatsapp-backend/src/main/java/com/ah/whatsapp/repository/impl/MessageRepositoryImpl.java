/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.MessageEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.MessageMapper;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.repository.MessageRepository;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import com.ah.whatsapp.repository.entity.MessageEntityRepository;
import com.ah.whatsapp.repository.entity.UserEntityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {
	private final MessageEntityRepository messageEntityRepository;
	private final ConversationEntityRepository conversationEntityRepository;
	private final UserEntityRepository userEntityRepository;
	private final MessageMapper messageMapper;

	@Override
	public Message save(Message message) {
		ConversationEntity conversationEntity =
				conversationEntityRepository
						.findById(message.getConversationId())
						.orElseThrow(
								() ->
										new ConversationNotFoundException(
												"Conversation not found: "
														+ message.getConversationId()));

		UserEntity senderEntity =
				userEntityRepository
						.findById(message.getSender().getId())
						.orElseThrow(
								() ->
										new UserNotFoundException(
												"User not found: " + message.getSender().getId()));

		MessageEntity messageEntity =
				messageMapper.toEntity(message, conversationEntity, senderEntity);

		MessageEntity savedEntity = messageEntityRepository.save(messageEntity);

		return messageMapper.toModel(savedEntity);
	}

	@Override
	public Optional<Message> findById(UUID id) {
		return messageEntityRepository.findById(id).map(messageMapper::toModel);
	}

	@Override
	public List<Message> findByConversationIdAndSentAtAfter(
			UUID conversationId, LocalDateTime sentAt) {
		return messageEntityRepository
				.findByConversationIdAndSendAtAfterOrderBySentAtAsc(conversationId, sentAt)
				.stream()
				.map(messageMapper::toModel)
				.toList();
	}

	@Override
	public void delete(UUID id) {
		messageEntityRepository.deleteById(id);
	}

	@Override
	public Optional<Message> findLatestByConversationId(UUID conversationId) {
		return messageEntityRepository
				.findByConversationIdOrderBySentAtDesc(conversationId)
				.map(messageMapper::toModel);
	}

	@Override
	public Map<UUID, Message> findLatestMessagesForConversations(List<UUID> conversationIds) {
		if (conversationIds == null || conversationIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<UUID> latestMessageIds =
				messageEntityRepository.findLatestMessageIdsForConversationIds(conversationIds);

		if (latestMessageIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<MessageEntity> latestMessagesWithSender =
				messageEntityRepository.findMessagesByIdsWithSender(latestMessageIds);

		// Group messages by conversation ID
		return latestMessagesWithSender.stream()
				.map(messageMapper::toModel)
				.collect(
						Collectors.toMap(
								Message::getConversationId,
								message -> message,
								(existing, replacement) -> existing));
	}
}
