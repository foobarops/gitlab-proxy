# Stage 1: Download dependencies
FROM gradle:8.9.0-jdk21-alpine AS dependencies
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml
# Use Gradle to download all dependencies needed for an offline build
RUN gradle --refresh-dependencies --no-daemon --info -a --build-cache --configuration-cache -x bootJar build

# Stage 2: Build the application using the cached dependencies
FROM gradle:8.9.0-jdk21-alpine AS builder
WORKDIR /app
# Copy cached dependencies from the previous stage
COPY --from=dependencies /home/gradle/.gradle /home/gradle/.gradle
COPY . .
# Build the application
RUN gradle --no-daemon --offline --info -a --build-cache --configuration-cache bootJar

# Stage 3: Create a minimal image to run the application
FROM eclipse-temurin:21-jre-alpine AS final
RUN addgroup -S gitlab-proxy && adduser -S gitlab-proxy -G gitlab-proxy
USER gitlab-proxy:gitlab-proxy
COPY --from=builder /app/build/libs/app.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]