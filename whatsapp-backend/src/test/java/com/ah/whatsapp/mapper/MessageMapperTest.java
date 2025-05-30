/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import static com.ah.whatsapp.mapper.MessageTestDataBuilder.aMessage;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.MessageEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;

public class MessageMapperTest {

	private MessageMapper messageMapper;
	private UserMapper userMapper;

	@BeforeEach
	public void setUp() {
		userMapper = new UserMapper();
		messageMapper = new MessageMapper(userMapper);
	}

	@Test
	public void testToEntity() {
		User sender = aUser().build();

		Message message = aMessage().withSender(sender).build();

		ConversationEntity conversationEntity = new ConversationEntity();
		conversationEntity.setId(message.getConversationId());

		UserEntity senderEntity = userMapper.toEntity(sender);

		MessageEntity result = messageMapper.toEntity(message, conversationEntity, senderEntity);

		assertEquals(message.getId(), result.getId());
		assertEquals(message.getContent(), result.getContent());
		assertEquals(message.getSentAt(), result.getSentAt());
		assertEquals(conversationEntity, result.getConversation());
		assertEquals(senderEntity, result.getSender());
	}

	@Test
	public void testToModel() {
		UserEntity senderEntity = aUser().buildEntity();

		ConversationEntity conversationEntity = new ConversationEntity();
		conversationEntity.setId(UUID.randomUUID());

		MessageEntity entity =
				aMessage()
						.withConversationEntity(conversationEntity)
						.withSenderEntity(senderEntity)
						.buildEntity();

		Message result = messageMapper.toModel(entity);

		assertEquals(entity.getId(), result.getId());
		assertEquals(entity.getContent(), result.getContent());
		assertEquals(entity.getSentAt(), result.getSentAt());
		assertEquals(conversationEntity.getId(), result.getConversationId());
		assertNotNull(result.getSender());
		assertEquals(senderEntity.getId(), result.getSender().getId());
		assertEquals(senderEntity.getName(), result.getSender().getName());
		assertEquals(senderEntity.getEmail(), result.getSender().getEmail());
	}

	@Test
	public void testToDto() {
		User sender = aUser().build();

		Message message = aMessage().withContent("Test message for DTO").withSender(sender).build();

		MessageDto result = messageMapper.toDto(message);

		assertEquals(message.getId(), result.id());
		assertEquals(message.getConversationId(), result.conversationId());
		assertEquals(message.getSender().getId(), result.senderId());
		assertEquals(message.getSender().getName(), result.senderName());
		assertEquals(message.getContent(), result.content());
		assertEquals(message.getSentAt(), result.sentAt());
	}

	@Test
	public void testToDto_WithNullSender() {
		Message message = aMessage().withContent("Test message").withSender(null).build();

		NullPointerException exception =
				assertThrows(
						NullPointerException.class,
						() -> {
							messageMapper.toDto(message);
						});
		assertNotNull(exception);
	}

	@Test
	public void testToEntity_WithNullValues() {
		Message message =
				aMessage()
						.withId(null)
						.withContent(null)
						.withSentAt(null)
						.withSender(aUser().build())
						.build();

		ConversationEntity conversationEntity = new ConversationEntity();
		UserEntity senderEntity = aUser().buildEntity();

		MessageEntity result = messageMapper.toEntity(message, conversationEntity, senderEntity);

		assertNull(result.getId());
		assertNull(result.getContent());
		assertNull(result.getSentAt());
		assertEquals(conversationEntity, result.getConversation());
		assertEquals(senderEntity, result.getSender());
	}
}
