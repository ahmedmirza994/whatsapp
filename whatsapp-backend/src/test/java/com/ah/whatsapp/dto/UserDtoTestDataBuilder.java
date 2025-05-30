/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

/**
 * Test Data Builder for UserDto record - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class UserDtoTestDataBuilder {

	private UUID id = UUID.randomUUID();
	private String name = "Test User";
	private String email = "test@example.com";
	private String phone = "+1234567890";
	private String profilePicture = "http://example.com/profile.jpg";
	private String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

	public static UserDtoTestDataBuilder aUserDto() {
		return new UserDtoTestDataBuilder();
	}

	public UserDtoTestDataBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public UserDtoTestDataBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public UserDtoTestDataBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public UserDtoTestDataBuilder withPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public UserDtoTestDataBuilder withProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
		return this;
	}

	public UserDtoTestDataBuilder withJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
		return this;
	}

	public UserDtoTestDataBuilder withNullValues() {
		this.name = null;
		this.phone = null;
		this.profilePicture = null;
		this.jwtToken = null;
		return this;
	}

	public UserDtoTestDataBuilder withNullToken() {
		this.jwtToken = null;
		return this;
	}

	public UserDto build() {
		return new UserDto(id, name, email, phone, profilePicture, jwtToken);
	}
}

// Usage Example:
// UserDto dto = aUserDto().withName("John Doe").withEmail("john@example.com").build();
// UserDto dtoWithoutToken = aUserDto().withNullToken().build();
