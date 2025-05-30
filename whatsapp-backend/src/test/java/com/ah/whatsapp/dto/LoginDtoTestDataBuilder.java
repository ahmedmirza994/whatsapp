/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

/**
 * Test Data Builder for LoginDto record - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class LoginDtoTestDataBuilder {

	private String email = "test@example.com";
	private String password = "password123";

	public static LoginDtoTestDataBuilder aLoginDto() {
		return new LoginDtoTestDataBuilder();
	}

	public LoginDtoTestDataBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public LoginDtoTestDataBuilder withPassword(String password) {
		this.password = password;
		return this;
	}

	public LoginDtoTestDataBuilder withNullValues() {
		this.email = null;
		this.password = null;
		return this;
	}

	public LoginDtoTestDataBuilder withValidCredentials() {
		this.email = "user@example.com";
		this.password = "securePassword123";
		return this;
	}

	public LoginDtoTestDataBuilder withInvalidEmail() {
		this.email = "invalid-email";
		return this;
	}

	public LoginDto build() {
		return new LoginDto(email, password);
	}
}

// Usage Example:
// LoginDto dto = aLoginDto().withEmail("user@example.com").withPassword("secret123").build();
// LoginDto validDto = aLoginDto().withValidCredentials().build();
