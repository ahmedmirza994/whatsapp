package com.ah.whatsapp.dto;

import com.ah.whatsapp.enums.EventType; // Assuming you create this enum
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent<T> {
    private EventType type;
    private T payload;
}
