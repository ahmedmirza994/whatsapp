package com.ah.whatsapp.repository;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationParticipantMapper;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import com.ah.whatsapp.repository.entity.ConversationParticipantEntityRepository;
import com.ah.whatsapp.repository.entity.UserEntityRepository;
import java.util.List;
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
			.findById(participant.getUser().getId())
			.orElseThrow(() -> new UserNotFoundException("User not found: " + participant.getUser().getId()));

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
	public List<ConversationParticipant> findByConversationId(UUID conversationId) {
		return participantEntityRepository.findByConversationId(conversationId).stream()
			.map(participantMapper::toModel)
			.collect(Collectors.toList());
	}

	@Override
	public List<ConversationParticipant> findByUserId(UUID userId) {
		return participantEntityRepository.findByUserId(userId).stream()
			.map(participantMapper::toModel)
			.collect(Collectors.toList());
	}

	@Override
	public Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId) {
		return participantEntityRepository.findByConversationIdAndUserId(conversationId, userId)
			.map(participantMapper::toModel);
	}

	@Override
	public boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId) {
		return participantEntityRepository.existsByConversationIdAndUserId(conversationId, userId);
	}

	@Override
	@Transactional
	public void delete(UUID id) {
		participantEntityRepository.deleteById(id);
	}
}
