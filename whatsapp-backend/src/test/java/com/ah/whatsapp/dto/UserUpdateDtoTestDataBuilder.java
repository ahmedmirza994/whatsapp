/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

/**
 * Test Data Builder for UserUpdateDto record - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class UserUpdateDtoTestDataBuilder {

	private String name = "Updated User";
	private String phone = "+1234567890";

	public static UserUpdateDtoTestDataBuilder aUserUpdateDto() {
		return new UserUpdateDtoTestDataBuilder();
	}

	public UserUpdateDtoTestDataBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public UserUpdateDtoTestDataBuilder withPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public UserUpdateDtoTestDataBuilder withNullValues() {
		this.name = null;
		this.phone = null;
		return this;
	}

	public UserUpdateDtoTestDataBuilder withNullName() {
		this.name = null;
		return this;
	}

	public UserUpdateDtoTestDataBuilder withNullPhone() {
		this.phone = null;
		return this;
	}

	public UserUpdateDto build() {
		return new UserUpdateDto(name, phone);
	}
}

// Usage Example:
// UserUpdateDto dto = aUserUpdateDto().withName("John Doe").withPhone("+9876543210").build();
// UserUpdateDto dtoWithNulls = aUserUpdateDto().withNullValues().build();
