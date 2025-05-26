plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "6.2.0.5505"
	id("com.diffplug.spotless") version "6.25.0"
	jacoco
}

group = "com.ah"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
	implementation("org.liquibase:liquibase-core")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")

	// Security
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

	implementation("net.datafaker:datafaker:2.4.3")

	// Test dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation("org.assertj:assertj-core")
	testImplementation("org.hamcrest:hamcrest")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}
}

sonarqube {
	properties {
		property("sonar.organization", System.getenv("SONAR_ORGANIZATION"))
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.projectKey", "ahmedmirza994_whatsapp")
		property("sonar.projectName", "WhatsApp Backend")
		property("sonar.projectVersion", "1.0")
		property("sonar.sources", "src/main/java")
		property("sonar.tests", "src/test/java")
		property("sonar.binaries", "build/classes/java/main")
		property("sonar.java.coveragePlugin", "jacoco")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
		property("sonar.java.source", "21")
		property("sonar.java.target", "21")
		property("sonar.sourceEncoding", "UTF-8")

		// Quality gate settings
		property("sonar.qualitygate.wait", "true")

		// Exclude generated files and configuration files
		property("sonar.exclusions", "**/generated/**/*,**/*.properties,**/*.yml,**/*.xml")
		property("sonar.test.exclusions", "**/test/**/*")
	}
}

// Spotless Configuration for Code Formatting
spotless {
	java {
		target("src/**/*.java")

		// Use Google Java Format with custom line length
		googleJavaFormat("1.22.0").aosp().reflowLongStrings().formatJavadoc(false)

		// Apply custom formatting rules
		custom("Line length adjustment") { content ->
			content
		}

		// Additional formatting rules
		removeUnusedImports()
		trimTrailingWhitespace()
		endWithNewline()

		// Optional: Custom import order
		importOrder("java", "javax", "org", "com", "")

		// Optional: License header
		licenseHeader(
			"""
			/*
			 * WhatsApp Clone - Backend Service
			 * Copyright (c) 2025
			 */
			""".trimIndent(),
		)
	}

	// Format Kotlin files (build.gradle.kts)
	kotlinGradle {
		target("*.gradle.kts")
		ktlint("0.50.0")
	}

	// Format other project files
	format("misc") {
		target("**/*.md", "**/.gitignore", "**/*.yml", "**/*.yaml", "**/*.properties")
		trimTrailingWhitespace()
		indentWithSpaces(4)
		endWithNewline()
	}
}

// Make build depend on spotless check
tasks.build {
	dependsOn(tasks.spotlessCheck)
}
