/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import static com.ah.whatsapp.mapper.ConversationParticipantTestDataBuilder.aConversationParticipant;
import static com.ah.whatsapp.mapper.ConversationTestDataBuilder.aConversation;
import static com.ah.whatsapp.mapper.MessageTestDataBuilder.aMessage;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.ParticipantDto;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;

public class ConversationMapperTest {

    private ConversationMapper conversationMapper;
    private MessageMapper messageMapper;
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        userMapper = new UserMapper();
        messageMapper = new MessageMapper(userMapper);
        conversationMapper = new ConversationMapper(messageMapper);
    }

    @Test
    public void testToEntity() {
        Conversation conversation = aConversation().build();

        ConversationEntity result = conversationMapper.toEntity(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertEquals(conversation.getCreatedAt(), result.getCreatedAt());
        assertEquals(conversation.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    public void testToModel() {
        ConversationEntity entity = aConversation().buildEntity();

        Conversation result = conversationMapper.toModel(entity);

        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getCreatedAt(), result.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    public void testCreateNewConversation() {
        Conversation result = conversationMapper.createNewConversation();

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(result.getCreatedAt(), result.getUpdatedAt());
        assertNotNull(result.getParticipants());
        assertTrue(result.getParticipants().isEmpty());
        assertNotNull(result.getMessages());
        assertTrue(result.getMessages().isEmpty());
        assertNull(result.getLastMessage());
    }

    @Test
    public void testToDto_WithAllFields() {
        List<ConversationParticipant> participants =
                List.of(
                        aConversationParticipant()
                                .withParticipantName("John Doe")
                                .withParticipantEmail("john@example.com")
                                .build(),
                        aConversationParticipant()
                                .withParticipantName("Jane Smith")
                                .withParticipantEmail("jane@example.com")
                                .build());

        Message testMessage =
                aMessage()
                        .withContent("Test message content")
                        .withSender(
                                aUser().withName("Test Sender")
                                        .withEmail("sender@example.com")
                                        .build())
                        .build();

        Conversation conversation =
                aConversation()
                        .withParticipants(participants)
                        .withMessages(List.of(testMessage))
                        .withLastMessage(testMessage)
                        .build();

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertEquals(conversation.getCreatedAt(), result.getCreatedAt());
        assertEquals(conversation.getUpdatedAt(), result.getUpdatedAt());

        assertNotNull(result.getParticipants());
        assertEquals(2, result.getParticipants().size());

        ParticipantDto participant1 = result.getParticipants().get(0);
        ConversationParticipant originalParticipant1 = conversation.getParticipants().get(0);
        assertEquals(originalParticipant1.getId(), participant1.id());
        assertEquals(originalParticipant1.getParticipantId(), participant1.userId());
        assertEquals(originalParticipant1.getParticipantEmail(), participant1.email());
        assertEquals(originalParticipant1.getParticipantName(), participant1.name());

        assertNotNull(result.getMessages());
        assertEquals(1, result.getMessages().size());

        MessageDto messageDto = result.getMessages().get(0);
        Message originalMessage = conversation.getMessages().get(0);
        assertEquals(originalMessage.getId(), messageDto.id());
        assertEquals(originalMessage.getContent(), messageDto.content());

        assertNotNull(result.getLastMessage());
        assertEquals(conversation.getLastMessage().getId(), result.getLastMessage().id());
    }

    @Test
    public void testToDto_WithNullParticipants() {
        Conversation conversation = aConversation().withParticipants(null).build();

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNotNull(result.getParticipants());
        assertTrue(result.getParticipants().isEmpty());
    }

    @Test
    public void testToDto_WithNullMessages() {
        Conversation conversation = aConversation().withMessages(null).build();

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNotNull(result.getMessages());
        assertTrue(result.getMessages().isEmpty());
    }

    @Test
    public void testToDto_WithNullLastMessage() {
        Conversation conversation = aConversation().withLastMessage(null).build();

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNull(result.getLastMessage());
    }

    @Test
    public void testToDto_WithEmptyParticipantsAndMessages() {
        Conversation conversation =
                aConversation()
                        .withParticipants(new ArrayList<>())
                        .withMessages(new ArrayList<>())
                        .build();

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNotNull(result.getParticipants());
        assertTrue(result.getParticipants().isEmpty());
        assertNotNull(result.getMessages());
        assertTrue(result.getMessages().isEmpty());
    }
}
