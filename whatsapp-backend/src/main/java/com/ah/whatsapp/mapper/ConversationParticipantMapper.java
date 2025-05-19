package com.ah.whatsapp.mapper;

import org.springframework.stereotype.Component;

import com.ah.whatsapp.dto.ParticipantDto;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConversationParticipantMapper {

    private final UserMapper userMapper;

    /**
     * Convert model to entity
     */
    public ConversationParticipantEntity toEntity(ConversationParticipant model, ConversationEntity conversationEntity, UserEntity userEntity) {
        ConversationParticipantEntity entity = new ConversationParticipantEntity();
        entity.setId(model.getId());
        entity.setConversation(conversationEntity);
        entity.setUser(userEntity);
        entity.setJoinedAt(model.getJoinedAt());
		entity.setLeftAt(model.getLeftAt());
	    entity.setActive(model.isActive());
        return entity;
    }

    /**
     * Convert entity to model
     */
    public ConversationParticipant toModel(ConversationParticipantEntity entity) {
        ConversationParticipant model = new ConversationParticipant();
        model.setId(entity.getId());
        model.setConversationId(entity.getConversation().getId());
	    UserEntity user = entity.getUser();
	    model.setParticipantId(user.getId());
		model.setParticipantEmail(user.getEmail());
		model.setParticipantName(user.getName());
		model.setParticipantProfilePicture(user.getProfilePicture());
        model.setJoinedAt(entity.getJoinedAt());
	    model.setActive(entity.isActive());
	    model.setLeftAt(entity.getLeftAt());
        return model;
    }

    /**
     * Convert model to DTO
     */
    public ParticipantDto toDto(ConversationParticipant model) {
        return new ParticipantDto(
            model.getId(),
            model.getParticipantId(),
	        model.getParticipantEmail(),
	        model.getParticipantName(),
	        model.getParticipantProfilePicture(),
            model.getJoinedAt()
        );
    }
}
