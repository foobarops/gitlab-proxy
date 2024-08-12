# Stage 1: Download dependencies
FROM gradle:8.9.0-jdk21-alpine AS dependencies
COPY gradle-wd /app/gradle-wd
# Use Gradle to download all dependencies needed for an offline build
RUN gradle build --refresh-dependencies --project-dir /app/gradle-wd --configuration-cache --build-cache -x bootJar -a --no-daemon --info

# Stage 2: Build the application using the cached dependencies
FROM gradle:8.9.0-jdk21-alpine AS builder
# Copy cached dependencies from the previous stage
COPY --from=dependencies /home/gradle/.gradle /home/gradle/.gradle
COPY . /app
# Build the application
RUN gradle bootJar --offline --project-dir /app/gradle-wd --configuration-cache --build-cache -a --no-daemon --info

# Stage 3: Create a minimal image to run the application
FROM eclipse-temurin:21-jre-alpine AS final
RUN addgroup -S gitlab-proxy && adduser -S gitlab-proxy -G gitlab-proxy && \
    mkdir /ehcache && chown gitlab-proxy:gitlab-proxy /ehcache
VOLUME /ehcache
USER gitlab-proxy:gitlab-proxy
COPY --from=builder /app/gradle-wd/build/libs/app.jar /app/app.jar
WORKDIR /app
ENTRYPOINT java $JAVA_OPTS -jar app.jar