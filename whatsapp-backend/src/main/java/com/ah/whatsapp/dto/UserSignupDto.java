package com.ah.whatsapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignupDto(
	@NotNull(message = "Name is required")
	@Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
	String name,
	@NotNull(message = "Email is required") 
	@Email(message = "Email must be valid") 
	String email,
	@NotNull(message = "Password is required")
	@Size(min = 8, max = 100, message = "Password must be between 6 and 100 characters")
	String password,
	@Pattern(regexp = "^\\+[0-9]{7,15}$", message = "Phone number is invalid") 
	String phone
) {
}
