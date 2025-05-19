package com.ah.whatsapp.service.impl;

import com.ah.whatsapp.event.ConversationUpdateEvent;
import com.ah.whatsapp.event.MessageDeletedEvent;
import com.ah.whatsapp.event.NewMessageEvent;
import com.ah.whatsapp.model.ConversationParticipant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.SendMessageRequest;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.MessageNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.mapper.MessageMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.MessageRepository;
import com.ah.whatsapp.repository.UserRepository;
import com.ah.whatsapp.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
	private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
	private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageMapper messageMapper;
	private final SimpMessagingTemplate messagingTemplate;
	private final ConversationMapper conversationMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	@Transactional
	public MessageDto sendMessage(SendMessageRequest request, UUID senderId) {
		User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        Conversation conversation = conversationRepository.findById(request.conversationId())
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

		if (!conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(request.conversationId(), senderId)) {
            throw new AccessDeniedException("User is not a participant in this conversation");
        }

		List<ConversationParticipant> participants = conversationParticipantRepository.findByConversationId(request.conversationId());

		for (ConversationParticipant participant : participants) {
			if (!participant.isActive()) {
				participant.setActive(true);
				participant.setJoinedAt(LocalDateTime.now());
				conversationParticipantRepository.save(participant);
			}
		}

        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSender(sender);
        message.setContent(request.content());
        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Update conversation last update timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

		MessageDto messageDto = messageMapper.toDto(savedMessage);

		eventPublisher.publishEvent(new NewMessageEvent(this, messageDto));

		Conversation updatedConversation = conversationRepository.findById(conversation.getId()).orElse(conversation); // Re-fetch or use existing
        ConversationDto conversationDto = conversationMapper.toDto(updatedConversation);

		eventPublisher.publishEvent(new ConversationUpdateEvent(this, conversationDto));

        return messageDto;
	}

	@Override
	public List<MessageDto> findConversationMessages(UUID conversationId, UUID userId) {
		if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException("Conversation not found");
        }

		if (!conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)) {
			throw new AccessDeniedException("User is not a participant in this conversation");
		}

		ConversationParticipant participant = conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)
			.orElseThrow(() -> new AccessDeniedException("User is not a participant in this conversation"));

		return messageRepository.findByConversationIdAndSentAtAfter(conversationId, participant.getJoinedAt()).stream()
            .map(messageMapper::toDto)
            .toList();
	}

	@Override
    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new MessageNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own messages.");
        }

		messageRepository.delete(messageId);

		eventPublisher.publishEvent(new MessageDeletedEvent(this, messageId, message.getConversationId()));
    }
}
