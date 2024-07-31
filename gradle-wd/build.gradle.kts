/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.spring.boot)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api(libs.org.springframework.boot.spring.boot.starter.web)
    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)
    api(libs.org.gitlab4j.gitlab4j.api)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "gitlab-proxy"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_21

val bootJarTaskFile = file("bootJar.tasks.gradle.kts")
if (bootJarTaskFile.exists()) {
    apply(from = bootJarTaskFile)
}

// Configure source sets
sourceSets {
    main {
        java {
            srcDirs("../src/main/java")
        }
        resources {
            srcDirs("../src/main/resources")
        }
    }
    test {
        java {
            srcDirs("../src/test/java")
        }
        resources {
            srcDirs("../src/test/resources")
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}