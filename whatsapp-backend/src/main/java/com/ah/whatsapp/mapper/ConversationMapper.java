package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.ParticipantDto;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;

@Component
public class ConversationMapper {
	private final MessageMapper messageMapper;

	public ConversationMapper(MessageMapper messageMapper) {
		this.messageMapper = messageMapper;
	}

	public ConversationEntity toEntity(Conversation model) {
        ConversationEntity entity = new ConversationEntity();
        BeanUtils.copyProperties(model, entity);
        return entity;
    }

    public Conversation toModel(ConversationEntity entity) {
        Conversation model = new Conversation();
        BeanUtils.copyProperties(entity, model);
        return model;
    }

    public Conversation createNewConversation() {
        Conversation conversation = new Conversation();
        LocalDateTime now = LocalDateTime.now();
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        return conversation;
    }

	public ConversationDto toDto(Conversation model) {
		ConversationDto dto = new ConversationDto();
		BeanUtils.copyProperties(model, dto);

		if(model.getParticipants() != null) {
			List<ParticipantDto> participantDtos = model.getParticipants().stream()
                .map(this::mapToParticipantDto)
                .collect(Collectors.toList());
            dto.setParticipants(participantDtos);
		}

		if (model.getMessages() != null && !model.getMessages().isEmpty()) {
            // Find the most recent message
            Message lastMessage = model.getMessages().stream()
                .max((m1, m2) -> m1.getSentAt().compareTo(m2.getSentAt()))
                .orElse(null);

            if (lastMessage != null) {
                dto.setLastMessage(messageMapper.toDto(lastMessage));
            }
        }

		return dto;
	}

	private ParticipantDto mapToParticipantDto(ConversationParticipant participant) {
        return new ParticipantDto(
            participant.getId(),
            participant.getUser().getId(),
            participant.getUser().getName(),
            participant.getUser().getProfilePictureUrl(),
            participant.getJoinedAt()
        );
    }
}
