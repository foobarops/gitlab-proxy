FROM eclipse-temurin:11-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package

# Second stage: Create a minimal image to run the application
FROM eclipse-temurin:11-jre-alpine AS final
RUN addgroup -S gitlab-proxy && adduser -S gitlab-proxy -G gitlab-proxy
USER gitlab-proxy:gitlab-proxy
COPY --chown=gitlab-proxy --from=builder /app/target/app.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]