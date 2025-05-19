package com.ah.whatsapp.repository.impl;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationParticipantMapper;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import com.ah.whatsapp.repository.entity.ConversationParticipantEntityRepository;
import com.ah.whatsapp.repository.entity.UserEntityRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationParticipantRepositoryImpl implements ConversationParticipantRepository {

	private final ConversationParticipantEntityRepository participantEntityRepository;
	private final ConversationEntityRepository conversationEntityRepository;
	private final UserEntityRepository userEntityRepository;
	private final ConversationParticipantMapper participantMapper;

	@Override
	@Transactional
	public ConversationParticipant save(ConversationParticipant participant) {
		// Get conversation entity
		ConversationEntity conversationEntity = conversationEntityRepository
			.findById(participant.getConversationId())
			.orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + participant.getConversationId()));

		// Get user entity
		UserEntity userEntity = userEntityRepository
			.findById(participant.getParticipantId())
			.orElseThrow(() -> new UserNotFoundException("User not found: " + participant.getParticipantId()));

		// Convert to entity
		ConversationParticipantEntity entity = participantMapper.toEntity(participant, conversationEntity, userEntity);

		// Save entity
		ConversationParticipantEntity savedEntity = participantEntityRepository.save(entity);

		// Convert back to model and return
		return participantMapper.toModel(savedEntity);
	}

	@Override
	public Optional<ConversationParticipant> findById(UUID id) {
		return participantEntityRepository.findById(id)
			.map(participantMapper::toModel);
	}

	@Override
	public List<ConversationParticipant> findByConversationIdAndIsActiveTrue(UUID conversationId) {
		return participantEntityRepository.findByConversationIdAndIsActiveTrueWithUser(conversationId).stream()
			.map(participantMapper::toModel)
			.collect(Collectors.toList());
	}

	@Override
	public boolean existsByConversationIdAndUserIdAndIsActiveTrue(UUID conversationId, UUID userId) {
		return participantEntityRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);
	}

	@Override
	public Map<UUID, List<ConversationParticipant>> findParticipantsForConversationsAndIsActiveTrue(List<UUID> conversationIds) {
		if (conversationIds == null || conversationIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<ConversationParticipantEntity> participantEntities = participantEntityRepository.findByConversationIdInAndIsActiveTrueWithUser(conversationIds);

		return participantEntities.stream()
			.map(participantMapper::toModel)
			.collect(Collectors.groupingBy(ConversationParticipant::getConversationId));
	}

	@Override
	public Optional<ConversationParticipant> findByConversationIdAndUserIdAndIsActiveTrue(UUID conversationId, UUID userId) {
		return participantEntityRepository
			.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)
			.map(participantMapper::toModel);
	}

	@Override
	public List<ConversationParticipant> findByConversationId(UUID conversationId) {
		return participantEntityRepository.findByConversationIdWithUser(conversationId).stream()
			.map(participantMapper::toModel)
			.collect(Collectors.toList());
	}

	@Override
	public Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId) {
		return participantEntityRepository
			.findByConversationIdAndUserId(conversationId, userId)
			.map(participantMapper::toModel);
	}
}
