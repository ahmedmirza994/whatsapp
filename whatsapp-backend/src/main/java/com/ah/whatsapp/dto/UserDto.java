package com.ah.whatsapp.dto;

import java.util.UUID;

public record UserDto(
	UUID id,
	String name,
	String email,
	String phone,
	String profilePictureUrl,
	String jwtToken
) {

}
