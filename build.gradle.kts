import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	alias(libs.plugins.kotlin)
	alias(libs.plugins.loom)
}

group = "net.ririfa"
version = "0.0.1"
description = "Let's draw distant chunks with minimal performance loss."

repositories {
	mavenCentral()
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.terraformersmc.com/")
	maven("https://maven.shedaniel.me/")
	maven("https://api.modrinth.com/maven")
}

dependencies{
	minecraft(libs.minecraft)
	mappings(libs.fabric.yarn)
	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.api)
	modImplementation(libs.fabric.kotlin)
	modImplementation(libs.modmenu)
	modImplementation(libs.sodium)
	modApi(libs.cloth)
	modApi(libs.langman)
	modApi(libs.gson)
}

loom {
	accessWidenerPath = file("src/main/resources/skyline.accesswidener")
}

val targetJavaVersion = 21

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
	}
	withSourcesJar()
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.release.set(targetJavaVersion)
}

kotlin {
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
	}
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_21)
	}
}

tasks.named<ProcessResources>("processResources") {
	inputs.property("version", project.version)
	inputs.property("description", project.description)

	filesMatching("fabric.mod.json") {
		expand(mapOf(
			"version" to project.version,
			"description" to project.description
		))
	}
}