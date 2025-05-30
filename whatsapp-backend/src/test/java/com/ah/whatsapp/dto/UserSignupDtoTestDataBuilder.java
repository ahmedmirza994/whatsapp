/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

/**
 * Test Data Builder for UserSignupDto record - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class UserSignupDtoTestDataBuilder {

	private String name = "Test User";
	private String email = "test@example.com";
	private String password = "password123";
	private String phone = "+1234567890";

	public static UserSignupDtoTestDataBuilder aUserSignupDto() {
		return new UserSignupDtoTestDataBuilder();
	}

	public UserSignupDtoTestDataBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public UserSignupDtoTestDataBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public UserSignupDtoTestDataBuilder withPassword(String password) {
		this.password = password;
		return this;
	}

	public UserSignupDtoTestDataBuilder withPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public UserSignupDtoTestDataBuilder withNullValues() {
		this.name = null;
		this.email = null;
		this.password = null;
		this.phone = null;
		return this;
	}

	public UserSignupDtoTestDataBuilder withValidData() {
		this.name = "John Doe";
		this.email = "john.doe@example.com";
		this.password = "securePassword123";
		this.phone = "+1234567890";
		return this;
	}

	public UserSignupDto build() {
		return new UserSignupDto(name, email, password, phone);
	}
}

// Usage Example:
// UserSignupDto dto = aUserSignupDto().withName("John Doe").withEmail("john@example.com").build();
// UserSignupDto validDto = aUserSignupDto().withValidData().build();
