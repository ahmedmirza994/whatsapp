package com.ah.whatsapp.mapper;

import org.springframework.stereotype.Component;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.MessageEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.Message;

@Component
public class MessageMapper {
	private final UserMapper userMapper;

    public MessageMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public MessageEntity toEntity(Message model, ConversationEntity conversationEntity, UserEntity senderEntity) {
        MessageEntity entity = new MessageEntity();
        entity.setId(model.getId());
        entity.setContent(model.getContent());
        entity.setSentAt(model.getSentAt());
        entity.setConversation(conversationEntity);
        entity.setSender(senderEntity);
        return entity;
    }

    public Message toModel(MessageEntity entity) {
        Message model = new Message();
        model.setId(entity.getId());
        model.setContent(entity.getContent());
        model.setSentAt(entity.getSentAt());
        model.setConversationId(entity.getConversation().getId());
        model.setSender(userMapper.toModel(entity.getSender()));
        return model;
    }

	public MessageDto toDto(Message model) {
		return new MessageDto(
			model.getId(),
			model.getConversationId(),
			model.getSender().getId(),
			model.getSender().getName(),
			model.getContent(),
			model.getSentAt()
		);
	}
}
