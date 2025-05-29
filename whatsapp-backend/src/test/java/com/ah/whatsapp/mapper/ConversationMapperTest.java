package com.ah.whatsapp.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.ParticipantDto;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;

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
        Conversation conversation = createTestConversation();

        ConversationEntity result = conversationMapper.toEntity(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertEquals(conversation.getCreatedAt(), result.getCreatedAt());
        assertEquals(conversation.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    public void testToModel() {
        ConversationEntity entity = new ConversationEntity();
        entity.setId(UUID.randomUUID());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

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
        Conversation conversation = createTestConversationWithParticipantsAndMessages();

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
        Conversation conversation = createTestConversation();
        conversation.setParticipants(null);

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNotNull(result.getParticipants());
        assertTrue(result.getParticipants().isEmpty());
    }

    @Test
    public void testToDto_WithNullMessages() {
        Conversation conversation = createTestConversation();
        conversation.setMessages(null);

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNotNull(result.getMessages());
        assertTrue(result.getMessages().isEmpty());
    }

    @Test
    public void testToDto_WithNullLastMessage() {
        Conversation conversation = createTestConversation();
        conversation.setLastMessage(null);

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNull(result.getLastMessage());
    }

    @Test
    public void testToDto_WithEmptyParticipantsAndMessages() {
        Conversation conversation = createTestConversation();
        conversation.setParticipants(new ArrayList<>());
        conversation.setMessages(new ArrayList<>());

        ConversationDto result = conversationMapper.toDto(conversation);

        assertEquals(conversation.getId(), result.getId());
        assertNotNull(result.getParticipants());
        assertTrue(result.getParticipants().isEmpty());
        assertNotNull(result.getMessages());
        assertTrue(result.getMessages().isEmpty());
    }

    private Conversation createTestConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversation.setParticipants(new ArrayList<>());
        conversation.setMessages(new ArrayList<>());
        return conversation;
    }

    private Conversation createTestConversationWithParticipantsAndMessages() {
        Conversation conversation = createTestConversation();

        List<ConversationParticipant> participants = new ArrayList<>();
        participants.add(createTestParticipant("John Doe", "john@example.com"));
        participants.add(createTestParticipant("Jane Smith", "jane@example.com"));
        conversation.setParticipants(participants);

        List<Message> messages = new ArrayList<>();
        Message message = createTestMessage();
        messages.add(message);
        conversation.setMessages(messages);

        conversation.setLastMessage(message);

        return conversation;
    }

    private ConversationParticipant createTestParticipant(String name, String email) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setId(UUID.randomUUID());
        participant.setConversationId(UUID.randomUUID());
        participant.setParticipantId(UUID.randomUUID());
        participant.setParticipantName(name);
        participant.setParticipantEmail(email);
        participant.setParticipantProfilePicture("http://example.com/profile.jpg");
        participant.setJoinedAt(LocalDateTime.now());
        participant.setActive(true);
        participant.setLeftAt(null);
        participant.setLastReadAt(LocalDateTime.now());
        return participant;
    }

    private Message createTestMessage() {
        User sender = new User();
        sender.setId(UUID.randomUUID());
        sender.setName("Test Sender");
        sender.setEmail("sender@example.com");

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setConversationId(UUID.randomUUID());
        message.setContent("Test message content");
        message.setSentAt(LocalDateTime.now());
        message.setSender(sender);
        return message;
    }
}
