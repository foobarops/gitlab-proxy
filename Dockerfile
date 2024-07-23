# Stage 1: Download dependencies
FROM maven:3.9.8-eclipse-temurin-11-alpine as dependencies
WORKDIR /app
COPY pom.xml .
# Use Maven wrapper to download all dependencies needed for an offline build
RUN mvn dependency:go-offline

# Stage 2: Build the application using the cached dependencies
FROM maven:3.9.8-eclipse-temurin-11-alpine as builder
WORKDIR /app
# Copy cached dependencies from the previous stage
COPY --from=dependencies /root/.m2 /root/.m2
COPY . .
# Build the application
RUN mvn package

# Stage 3: Create a minimal image to run the application
FROM eclipse-temurin:11-jre-alpine AS final
RUN addgroup -S gitlab-proxy && adduser -S gitlab-proxy -G gitlab-proxy
USER gitlab-proxy:gitlab-proxy
COPY --from=builder /app/target/app.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]