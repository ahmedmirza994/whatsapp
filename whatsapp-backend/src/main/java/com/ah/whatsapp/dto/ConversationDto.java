package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
	private UUID id;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<ParticipantDto> participants = new ArrayList<>();
	private List<MessageDto> messages = new ArrayList<>();
	private MessageDto lastMessage;
}
