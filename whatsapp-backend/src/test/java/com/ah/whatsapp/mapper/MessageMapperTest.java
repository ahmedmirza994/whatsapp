package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        User sender = createTestUser();

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setContent("Test message content");
        message.setSentAt(LocalDateTime.now());
        message.setConversationId(UUID.randomUUID());
        message.setSender(sender);

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
        UserEntity senderEntity = createTestUserEntity();

        ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setId(UUID.randomUUID());

        MessageEntity entity = new MessageEntity();
        entity.setId(UUID.randomUUID());
        entity.setContent("Test message content");
        entity.setSentAt(LocalDateTime.now());
        entity.setConversation(conversationEntity);
        entity.setSender(senderEntity);

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
        User sender = createTestUser();

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setContent("Test message for DTO");
        message.setSentAt(LocalDateTime.now());
        message.setConversationId(UUID.randomUUID());
        message.setSender(sender);

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
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setContent("Test message");
        message.setSentAt(LocalDateTime.now());
        message.setConversationId(UUID.randomUUID());
        message.setSender(null);

        assertThrows(NullPointerException.class, () -> {
            messageMapper.toDto(message);
        });
    }

    @Test
    public void testToEntity_WithNullValues() {
        Message message = new Message();
        message.setId(null);
        message.setContent(null);
        message.setSentAt(null);
        message.setConversationId(UUID.randomUUID());
        message.setSender(createTestUser());

        ConversationEntity conversationEntity = new ConversationEntity();
        UserEntity senderEntity = createTestUserEntity();

        MessageEntity result = messageMapper.toEntity(message, conversationEntity, senderEntity);

        assertNull(result.getId());
        assertNull(result.getContent());
        assertNull(result.getSentAt());
        assertEquals(conversationEntity, result.getConversation());
        assertEquals(senderEntity, result.getSender());
    }

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPhone("+1234567890");
        user.setProfilePicture("http://example.com/profile.jpg");
        return user;
    }

    private UserEntity createTestUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setName("Jane Smith");
        userEntity.setEmail("jane.smith@example.com");
        userEntity.setPhone("+0987654321");
        userEntity.setProfilePicture("http://example.com/jane.jpg");
        return userEntity;
    }
}
