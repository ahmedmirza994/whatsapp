package com.ah.whatsapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ah.whatsapp.constant.WebSocketConstants;
import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.SendMessageRequest;
import com.ah.whatsapp.dto.WebSocketEvent;
import com.ah.whatsapp.enums.EventType;
import com.ah.whatsapp.exception.ConversationNotFoundException;
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

import static com.ah.whatsapp.constant.WebSocketConstants.CONVERSATION_QUEUE;

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

	@Override
	@Transactional
	public MessageDto sendMessage(SendMessageRequest request, UUID senderId) {
		User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        Conversation conversation = conversationRepository.findById(request.conversationId())
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

		if (!conversationParticipantRepository.existsByConversationIdAndUserId(request.conversationId(), senderId)) {
            throw new AccessDeniedException("User is not a participant in this conversation");
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

		WebSocketEvent<MessageDto> event = new WebSocketEvent<>(EventType.NEW_MESSAGE, messageDto);

        String destination = String.format(WebSocketConstants.CONVERSATION_TOPIC_TEMPLATE, messageDto.conversationId());
        log.info("Broadcasting event to destination: {}", destination);
        messagingTemplate.convertAndSend(destination, event);

		Conversation updatedConversation = conversationRepository.findById(conversation.getId()).orElse(conversation); // Re-fetch or use existing
        ConversationDto conversationDto = conversationMapper.toDto(updatedConversation);

		WebSocketEvent<ConversationDto> conversationUpdateEvent = new WebSocketEvent<>(EventType.CONVERSATION_UPDATE, conversationDto);
        String userQueueDestination = CONVERSATION_QUEUE; // Destination relative to user prefix

        conversationDto.getParticipants().forEach(participant -> {
            String participantEmail = participant.email(); // Assuming email is the username used in Principal
            if (participantEmail != null) {
                 log.info("Sending CONVERSATION_UPDATE event to user: {} at destination: {}{}", participantEmail, "/user/" + participantEmail, userQueueDestination);
                 messagingTemplate.convertAndSendToUser(participantEmail, userQueueDestination, conversationUpdateEvent);
            } else {
                 log.warn("Cannot send CONVERSATION_UPDATE to participant {} as email (username) is null.", participant.id());
            }
        });

        return messageDto;
	}

	@Override
	public List<MessageDto> findConversationMessages(UUID conversationId, UUID userId) {
		if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException("Conversation not found");
        }

		if (!conversationParticipantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
			throw new AccessDeniedException("User is not a participant in this conversation");
		}

		return messageRepository.findByConversationId(conversationId).stream()
            .map(messageMapper::toDto)
            .toList();
	}


}
