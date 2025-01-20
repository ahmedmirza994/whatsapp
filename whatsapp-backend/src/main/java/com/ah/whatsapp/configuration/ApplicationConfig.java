package com.ah.whatsapp.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.ah.whatsapp")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.ah.whatsapp.repository")
public class ApplicationConfig {
    
}
