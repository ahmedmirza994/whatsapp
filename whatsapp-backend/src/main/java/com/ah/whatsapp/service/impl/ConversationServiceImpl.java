package com.ah.whatsapp.service.impl;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.event.ConversationUpdateEvent;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.ConversationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;
	private final ConversationMapper conversationMapper;
	private final ConversationParticipantRepository conversationParticipantRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	@Transactional
	public ConversationDto createConversation(CreateConversationRequest request, UUID creatorId) {
		User creator = userRepository.findById(creatorId)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		User participant = userRepository.findById(request.participantId())
			.orElseThrow(() -> new UserNotFoundException("Participant user not found"));

		Conversation conversation = conversationMapper.createNewConversation();
		Conversation savedConversation = conversationRepository.save(conversation);

		// Add participants
		addParticipant(savedConversation.getId(), creator.getId());
		addParticipant(savedConversation.getId(), participant.getId());

		Conversation fullConversation = conversationRepository.findById(savedConversation.getId())
			.orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
		return conversationMapper.toDto(fullConversation);
	}

	@Override
	@Transactional
	public void addParticipant(UUID conversationId, UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found"));

		boolean alreadyExists = conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);

		if (!alreadyExists) {
			addParticipantInternal(conversationId, user);
		} else {
			log.warn("User {} is already a participant in conversation {}", userId, conversationId);
		}
	}

	private void addParticipantInternal(UUID conversationId, User user) {
		ConversationParticipant participant = new ConversationParticipant();
		participant.setConversationId(conversationId);
		participant.setParticipantId(user.getId());
		participant.setParticipantName(user.getName());
		participant.setJoinedAt(LocalDateTime.now());
		participant.setActive(true);

		conversationParticipantRepository.save(participant);
		log.info("Added participant {} ({}) to conversation {}", user.getId(), user.getName(), conversationId);
	}

	@Override
	public List<ConversationDto> findUserConversations(UUID userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserNotFoundException("User not found");
		}

		return conversationRepository.findByUserId(userId).stream()
			.map(conversationMapper::toDto)
			.toList();
	}

	@Override
	public ConversationDto findConversationByIdAndUser(UUID conversationId, UUID userId)
		throws ConversationNotFoundException, AccessDeniedException {

		// Check if user exists (optional, but good practice)
		if (!userRepository.existsById(userId)) {
			throw new UserNotFoundException("User requesting conversation not found");
		}

		// Check if the user is a participant
		if (!conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)) {
			// Throw AccessDeniedException if user is not part of the conversation
			throw new AccessDeniedException("User is not a participant in this conversation");
		}

		// Fetch the conversation; findById already loads participants and last message
		Conversation conversation = conversationRepository.findById(conversationId)
			.orElseThrow(() -> new ConversationNotFoundException("Conversation not found with id: " + conversationId));

		return conversationMapper.toDto(conversation);
	}

	@Override
	@Transactional
	public ConversationDto findOrCreateConversation(CreateConversationRequest createConversationRequest, UUID creatorId) {
		UUID participantId = createConversationRequest.participantId();
		log.info("Attempting to find or create conversation between {} and {}", creatorId, participantId);
		// Check if users exist first
		if (!userRepository.existsById(creatorId)) {
			throw new UserNotFoundException("Creator user not found: " + creatorId);
		}
		if (!userRepository.existsById(participantId)) {
			throw new UserNotFoundException("Participant user not found: " + participantId);
		}

		// Try to find an existing direct conversation
		Optional<Conversation> existingConversation = conversationRepository.findDirectConversationBetweenUsers(
			creatorId,
			participantId
		);

		if (existingConversation.isPresent()) {
			Conversation conversation = existingConversation.get();

			ConversationParticipant participant = conversation.getParticipants().stream().filter(p -> p.getParticipantId().equals(creatorId))
				.findFirst().orElse(null);
			if (participant != null && !participant.isActive()) {
				participant.setActive(true);
				participant.setJoinedAt(LocalDateTime.now());
				participant.setLeftAt(null);
				conversationParticipantRepository.save(participant);
				conversation = conversationRepository.findById(conversation.getId())
					.orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
			}
			return conversationMapper.toDto(conversation);

		} else {
			log.info("No existing direct conversation found. Creating new one between {} and {}", creatorId, participantId);
			// The createConversation method now handles adding both participants
			return createConversation(createConversationRequest, creatorId);
		}
	}

	@Override
	@Transactional
	public void deleteConversationForUser(UUID conversationId, UUID userId) {
		ConversationParticipant participant = conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)
			.orElseThrow(() -> new AccessDeniedException("User is not a participant in this conversation"));

		// Mark the participant as inactive instead of deleting
		participant.setActive(false);
		participant.setLeftAt(LocalDateTime.now());
		conversationParticipantRepository.save(participant);

		List<ConversationParticipant> participants = conversationParticipantRepository.findByConversationId(conversationId);
		if (participants.stream().noneMatch(ConversationParticipant::isActive)) {
			conversationRepository.delete(conversationId);
		}
	}

	@Override
	@Transactional
	public void markConversationAsRead(UUID conversationId, UUID userId) {
		ConversationParticipant participant = conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)
			.orElseThrow(() -> new AccessDeniedException("User is not a participant in this conversation"));

		participant.setLastReadAt(LocalDateTime.now());
		conversationParticipantRepository.save(participant);

		ConversationDto conversation = conversationMapper.toDto(conversationRepository.findById(conversationId)
			.orElseThrow(() -> new ConversationNotFoundException("Conversation not found with id: " + conversationId)));

		applicationEventPublisher.publishEvent(new ConversationUpdateEvent(this, conversation));
	}
}
