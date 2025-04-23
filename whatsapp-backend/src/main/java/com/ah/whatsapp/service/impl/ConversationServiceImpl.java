package com.ah.whatsapp.service.impl;

import com.ah.whatsapp.repository.ConversationParticipantRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.ConversationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
	private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
	private final ConversationParticipantRepository conversationParticipantRepository;

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
		Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversationId(conversationId);
        participant.setParticipantId(userId);
		participant.setParticipantName(user.getName());
        participant.setJoinedAt(LocalDateTime.now());

		conversationParticipantRepository.save(participant);
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
}
