import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.example"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.0")
    }
}

apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"

    `java-library`
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}