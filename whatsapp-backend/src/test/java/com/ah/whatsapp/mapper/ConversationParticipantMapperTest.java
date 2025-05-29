package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.ConversationParticipant;

public class ConversationParticipantMapperTest {

    private ConversationParticipantMapper participantMapper;

    @BeforeEach
    public void setUp() {
        participantMapper = new ConversationParticipantMapper();
    }

    @Test
    public void testToEntity() {
        ConversationParticipant model = createTestParticipantModel();
        ConversationEntity conversationEntity = createTestConversationEntity();
        UserEntity userEntity = createTestUserEntity();

        ConversationParticipantEntity result = participantMapper.toEntity(
                model, conversationEntity, userEntity);

        assertEquals(model.getId(), result.getId());
        assertEquals(conversationEntity, result.getConversation());
        assertEquals(userEntity, result.getUser());
        assertEquals(model.getJoinedAt(), result.getJoinedAt());
        assertEquals(model.getLeftAt(), result.getLeftAt());
        assertEquals(model.isActive(), result.isActive());
        assertEquals(model.getLastReadAt(), result.getLastReadAt());
    }

    @Test
    public void testToModel() {
        ConversationParticipantEntity entity = createTestParticipantEntity();

        ConversationParticipant result = participantMapper.toModel(entity);

        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getConversation().getId(), result.getConversationId());
        assertEquals(entity.getUser().getId(), result.getParticipantId());
        assertEquals(entity.getUser().getEmail(), result.getParticipantEmail());
        assertEquals(entity.getUser().getName(), result.getParticipantName());
        assertEquals(entity.getUser().getProfilePicture(), result.getParticipantProfilePicture());
        assertEquals(entity.getJoinedAt(), result.getJoinedAt());
        assertEquals(entity.isActive(), result.isActive());
        assertEquals(entity.getLeftAt(), result.getLeftAt());
        assertEquals(entity.getLastReadAt(), result.getLastReadAt());
    }

    @Test
    public void testToEntity_WithNullId() {
        ConversationParticipant model = createTestParticipantModel();
        model.setId(null);
        ConversationEntity conversationEntity = createTestConversationEntity();
        UserEntity userEntity = createTestUserEntity();

        ConversationParticipantEntity result = participantMapper.toEntity(
                model, conversationEntity, userEntity);

        assertNull(result.getId());
        assertEquals(conversationEntity, result.getConversation());
        assertEquals(userEntity, result.getUser());
    }

    @Test
    public void testToEntity_WithNullDates() {
        ConversationParticipant model = createTestParticipantModel();
        model.setJoinedAt(null);
        model.setLeftAt(null);
        model.setLastReadAt(null);
        ConversationEntity conversationEntity = createTestConversationEntity();
        UserEntity userEntity = createTestUserEntity();

        ConversationParticipantEntity result = participantMapper.toEntity(
                model, conversationEntity, userEntity);

        assertEquals(model.getId(), result.getId());
        assertNull(result.getJoinedAt());
        assertNull(result.getLeftAt());
        assertNull(result.getLastReadAt());
    }

    @Test
    public void testToModel_WithInactiveParticipant() {
        ConversationParticipantEntity entity = createTestParticipantEntity();
        entity.setActive(false);
        entity.setLeftAt(LocalDateTime.now());

        ConversationParticipant result = participantMapper.toModel(entity);

        assertEquals(entity.getId(), result.getId());
        assertFalse(result.isActive());
        assertEquals(entity.getLeftAt(), result.getLeftAt());
    }

    @Test
    public void testToModel_WithNullUserProfilePicture() {
        ConversationParticipantEntity entity = createTestParticipantEntity();
        entity.getUser().setProfilePicture(null);

        ConversationParticipant result = participantMapper.toModel(entity);

        assertEquals(entity.getId(), result.getId());
        assertNull(result.getParticipantProfilePicture());
    }

    @Test
    public void testRoundTripConversion() {
        ConversationParticipant originalModel = createTestParticipantModel();
        ConversationEntity conversationEntity = createTestConversationEntity();
        UserEntity userEntity = createTestUserEntity();

        conversationEntity.setId(originalModel.getConversationId());
        userEntity.setId(originalModel.getParticipantId());

        ConversationParticipantEntity entity = participantMapper.toEntity(
                originalModel, conversationEntity, userEntity);
        ConversationParticipant resultModel = participantMapper.toModel(entity);

        assertEquals(originalModel.getId(), resultModel.getId());
        assertEquals(originalModel.getConversationId(), resultModel.getConversationId());
        assertEquals(originalModel.getParticipantId(), resultModel.getParticipantId());
        assertEquals(originalModel.isActive(), resultModel.isActive());
        assertEquals(originalModel.getJoinedAt(), resultModel.getJoinedAt());
        assertEquals(originalModel.getLeftAt(), resultModel.getLeftAt());
        assertEquals(originalModel.getLastReadAt(), resultModel.getLastReadAt());

        assertEquals(userEntity.getName(), resultModel.getParticipantName());
        assertEquals(userEntity.getEmail(), resultModel.getParticipantEmail());
        assertEquals(userEntity.getProfilePicture(), resultModel.getParticipantProfilePicture());
    }

    private ConversationParticipant createTestParticipantModel() {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setId(UUID.randomUUID());
        participant.setConversationId(UUID.randomUUID());
        participant.setParticipantId(UUID.randomUUID());
        participant.setParticipantName("Original Name");
        participant.setParticipantEmail("original@example.com");
        participant.setParticipantProfilePicture("http://example.com/original.jpg");
        participant.setJoinedAt(LocalDateTime.now().minusDays(1));
        participant.setActive(true);
        participant.setLeftAt(null);
        participant.setLastReadAt(LocalDateTime.now().minusHours(1));
        return participant;
    }

    private ConversationEntity createTestConversationEntity() {
        ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setId(UUID.randomUUID());
        conversationEntity.setCreatedAt(LocalDateTime.now().minusDays(2));
        conversationEntity.setUpdatedAt(LocalDateTime.now());
        return conversationEntity;
    }

    private UserEntity createTestUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setName("John Doe");
        userEntity.setEmail("john.doe@example.com");
        userEntity.setPhone("+1234567890");
        userEntity.setProfilePicture("http://example.com/john.jpg");
        return userEntity;
    }

    private ConversationParticipantEntity createTestParticipantEntity() {
        ConversationParticipantEntity entity = new ConversationParticipantEntity();
        entity.setId(UUID.randomUUID());
        entity.setConversation(createTestConversationEntity());
        entity.setUser(createTestUserEntity());
        entity.setJoinedAt(LocalDateTime.now().minusDays(1));
        entity.setActive(true);
        entity.setLeftAt(null);
        entity.setLastReadAt(LocalDateTime.now().minusHours(2));
        return entity;
    }
}
