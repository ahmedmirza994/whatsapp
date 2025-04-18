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
        return entity;
    }

    /**
     * Convert entity to model
     */
    public ConversationParticipant toModel(ConversationParticipantEntity entity) {
        ConversationParticipant model = new ConversationParticipant();
        model.setId(entity.getId());
        model.setConversationId(entity.getConversation().getId());
        model.setUser(userMapper.toModel(entity.getUser()));
        model.setJoinedAt(entity.getJoinedAt());
        return model;
    }

    /**
     * Convert model to DTO
     */
    public ParticipantDto toDto(ConversationParticipant model) {
        User user = model.getUser();
        return new ParticipantDto(
            model.getId(),
            user.getId(),
            user.getName(),
            user.getProfilePictureUrl(),
            model.getJoinedAt()
        );
    }
}
