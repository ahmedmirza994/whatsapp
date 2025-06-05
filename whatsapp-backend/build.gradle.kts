plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "6.2.0.5505"
	id("com.diffplug.spotless") version "7.0.4"
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

// Create integration test source set
sourceSets {
	create("integrationTest") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
	}
}

val integrationTestImplementation by configurations.getting {
	extendsFrom(configurations.implementation.get())
}

val integrationTestRuntimeOnly by configurations.getting {
	extendsFrom(configurations.runtimeOnly.get())
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
	compileOnly("org.projectlombok:lombok:1.18.38")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok:1.18.38")

	// Security
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

	implementation("net.datafaker:datafaker:2.4.3")
	// Test dependencies (Unit tests only)
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation("org.assertj:assertj-core")
	testImplementation("org.hamcrest:hamcrest")

	// Integration test dependencies
	integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
	integrationTestImplementation("org.springframework.security:spring-security-test")
	integrationTestImplementation("org.assertj:assertj-core")
	integrationTestImplementation("org.hamcrest:hamcrest")

	// Testcontainers for integration testing
	integrationTestImplementation("org.testcontainers:junit-jupiter")
	integrationTestImplementation("org.testcontainers:postgresql")
	integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
}

tasks.withType<JavaCompile> {
	options.compilerArgs.addAll(listOf("-parameters"))
}

// Unit tests configuration
tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

// Integration tests task
val integrationTest = task<Test>("integrationTest") {
	description = "Runs integration tests."
	group = "verification"

	testClassesDirs = sourceSets["integrationTest"].output.classesDirs
	classpath = sourceSets["integrationTest"].runtimeClasspath
	shouldRunAfter("test")

	useJUnitPlatform()

	// Set system properties for TestContainers
	systemProperty("testcontainers.reuse.enable", "true")

	// Configure test execution
	maxParallelForks = 1 // Integration tests should run sequentially
	failFast = false

	// Reports configuration
	reports {
		html.required.set(true)
		junitXml.required.set(true)
	}
}

// Make check task depend on integration tests
tasks.check { dependsOn(integrationTest) }

tasks.jacocoTestReport {
	dependsOn(tasks.test, integrationTest)
	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}

	// Include both unit and integration test execution data
	executionData.setFrom(
		fileTree(project.buildDir.absolutePath).include("jacoco/*.exec")
	)

	// Configure source sets to include both main and test sources
	sourceDirectories.setFrom(files("src/main/java"))
	classDirectories.setFrom(files("build/classes/java/main"))
}

sonarqube {
	properties {
		property("sonar.organization", System.getenv("SONAR_ORGANIZATION"))
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.projectKey", "ahmedmirza994_whatsapp")
		property("sonar.projectName", "WhatsApp Backend")
		property("sonar.projectVersion", "1.0")
		property("sonar.sources", "src/main/java")
		// Include both unit and integration tests
		property("sonar.tests", "src/test/java,src/integrationTest/java")
		property("sonar.binaries", "build/classes/java/main")
		property("sonar.java.coveragePlugin", "jacoco")
		// Use the combined coverage report that includes both unit and integration tests
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
		property("sonar.java.source", "21")
		property("sonar.java.target", "21")
		property("sonar.sourceEncoding", "UTF-8")

		// Quality gate settings
		property("sonar.qualitygate.wait", "true")

		// Exclude generated files and configuration files
		property("sonar.exclusions", "**/generated/**/*,**/*.properties,**/*.yml,**/*.xml")
		property("sonar.test.exclusions", "**/test/**/*,**/integrationTest/**/*")
	}
}

// Spotless Configuration for Code Formatting
spotless {
	java {
		target("src/**/*.java")

		googleJavaFormat("1.27.0").aosp().reflowLongStrings().formatJavadoc(false)

		// Additional formatting rules
		removeUnusedImports()
		trimTrailingWhitespace()
		leadingSpacesToTabs(4)
		endWithNewline()
		formatAnnotations()

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
