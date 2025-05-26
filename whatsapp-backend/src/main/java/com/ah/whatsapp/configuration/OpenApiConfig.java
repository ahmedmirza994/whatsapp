/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI configureOpenApi() {
        final String securityScheme = "bearerAuth";
        return new OpenAPI()
                .info(
                        new Info()
                                .title("WhatsApp API")
                                .version("1.0")
                                .description(
                                        "API documentation for WhatsApp Clone backend (Spring Boot"
                                                + " 3.x, JWT, WebSocket, PostgreSQL)"))
                .addSecurityItem(new SecurityRequirement().addList(securityScheme))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securityScheme,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("JWT Bearer Token")));
    }
}
