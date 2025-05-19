package com.ah.whatsapp.dto;

import com.ah.whatsapp.enums.EventType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDto {
	private UUID conversationId;
	private UUID userId;
	private EventType eventType;
}
