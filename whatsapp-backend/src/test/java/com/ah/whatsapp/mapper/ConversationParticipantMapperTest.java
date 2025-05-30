/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import static com.ah.whatsapp.mapper.ConversationEntityTestDataBuilder.aConversationEntity;
import static com.ah.whatsapp.mapper.ConversationParticipantTestDataBuilder.aConversationParticipant;
import static com.ah.whatsapp.mapper.MapperAssertions.assertConversationParticipantEntityMatchesModel;
import static com.ah.whatsapp.mapper.MapperAssertions.assertConversationParticipantModelMatchesEntity;
import static com.ah.whatsapp.mapper.MapperAssertions.assertConversationParticipantsAreEquivalent;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.ConversationParticipant;

public class ConversationParticipantMapperTest {

	private ConversationParticipantMapper participantMapper;

	private static final UUID PARTICIPANT_ID =
			UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
	private static final UUID CONVERSATION_ID =
			UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
	private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");

	@BeforeEach
	public void setUp() {
		participantMapper = new ConversationParticipantMapper();
	}

	@Test
	public void testToEntity() {
		ConversationParticipant model =
				aConversationParticipant()
						.withId(PARTICIPANT_ID)
						.withConversationId(CONVERSATION_ID)
						.withParticipantId(USER_ID)
						.build();
		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity = aUser().withId(USER_ID).buildEntity();

		ConversationParticipantEntity result =
				participantMapper.toEntity(model, conversationEntity, userEntity);

		assertConversationParticipantModelMatchesEntity(model, result);
	}

	@Test
	public void testToModel() {
		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity =
				aUser().withId(USER_ID)
						.withName("John Doe")
						.withEmail("john.doe@example.com")
						.withProfilePicture("http://example.com/john.jpg")
						.buildEntity();
		ConversationParticipantEntity entity =
				aConversationParticipant()
						.withId(PARTICIPANT_ID)
						.buildEntity(conversationEntity, userEntity);

		ConversationParticipant result = participantMapper.toModel(entity);

		assertConversationParticipantEntityMatchesModel(entity, result);
	}

	@Test
	public void testToEntity_WithNullId() {
		ConversationParticipant model =
				aConversationParticipant()
						.withId(null)
						.withConversationId(CONVERSATION_ID)
						.withParticipantId(USER_ID)
						.build();
		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity = aUser().withId(USER_ID).buildEntity();

		ConversationParticipantEntity result =
				participantMapper.toEntity(model, conversationEntity, userEntity);

		assertNull(result.getId());
		assertEquals(conversationEntity, result.getConversation());
		assertEquals(userEntity, result.getUser());
	}

	@Test
	public void testToEntity_WithNullDates() {
		ConversationParticipant model =
				aConversationParticipant()
						.withId(PARTICIPANT_ID)
						.withConversationId(CONVERSATION_ID)
						.withParticipantId(USER_ID)
						.withNullDates()
						.build();
		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity = aUser().withId(USER_ID).buildEntity();

		ConversationParticipantEntity result =
				participantMapper.toEntity(model, conversationEntity, userEntity);

		assertEquals(model.getId(), result.getId());
		assertNull(result.getJoinedAt());
		assertNull(result.getLeftAt());
		assertNull(result.getLastReadAt());
	}

	@Test
	public void testToModel_WithInactiveParticipant() {
		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity = aUser().withId(USER_ID).buildEntity();
		ConversationParticipantEntity entity =
				aConversationParticipant()
						.withId(PARTICIPANT_ID)
						.withInactiveState()
						.buildEntity(conversationEntity, userEntity);

		ConversationParticipant result = participantMapper.toModel(entity);

		assertEquals(entity.getId(), result.getId());
		assertFalse(result.isActive());
		assertEquals(entity.getLeftAt(), result.getLeftAt());
	}

	@Test
	public void testToModel_WithNullUserProfilePicture() {
		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity = aUser().withId(USER_ID).withProfilePicture(null).buildEntity();
		ConversationParticipantEntity entity =
				aConversationParticipant()
						.withId(PARTICIPANT_ID)
						.buildEntity(conversationEntity, userEntity);

		ConversationParticipant result = participantMapper.toModel(entity);

		assertEquals(entity.getId(), result.getId());
		assertNull(result.getParticipantProfilePicture());
	}

	@Test
	public void testRoundTripConversion() {
		ConversationParticipant originalModel =
				aConversationParticipant()
						.withId(PARTICIPANT_ID)
						.withConversationId(CONVERSATION_ID)
						.withParticipantId(USER_ID)
						.withParticipantName("John Doe")
						.withParticipantEmail("john.doe@example.com")
						.withParticipantProfilePicture("http://example.com/john.jpg")
						.build();

		ConversationEntity conversationEntity =
				aConversationEntity().withId(CONVERSATION_ID).build();
		UserEntity userEntity =
				aUser().withId(USER_ID)
						.withName("John Doe")
						.withEmail("john.doe@example.com")
						.withProfilePicture("http://example.com/john.jpg")
						.buildEntity();

		ConversationParticipantEntity entity =
				participantMapper.toEntity(originalModel, conversationEntity, userEntity);
		ConversationParticipant resultModel = participantMapper.toModel(entity);

		assertConversationParticipantsAreEquivalent(originalModel, resultModel);
	}
}
