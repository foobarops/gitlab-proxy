FROM eclipse-temurin:11-jdk-alpine as builder
ARG BUILD_DIR=/tmp/build
COPY . ${BUILD_DIR}
WORKDIR ${BUILD_DIR}
ARG RESULT_DIR=/tmp/result
RUN ./mvnw package && \
    unzip -d ${RESULT_DIR} ${BUILD_DIR}/target/*.jar

# Second stage: Create a minimal image to run the application
FROM eclipse-temurin:11-jre-alpine
RUN addgroup -S gitlab-proxy && adduser -S gitlab-proxy -G gitlab-proxy
USER gitlab-proxy:gitlab-proxy
# Re-declare ARG BUILDER_RESULT_DIR for use in this stage
ARG BUILDER_RESULT_DIR=/tmp/result
# Use the ARG in COPY command
COPY --chown=gitlab-proxy --from=builder ${BUILDER_RESULT_DIR}/BOOT-INF/lib /app/lib
COPY --chown=gitlab-proxy --from=builder ${BUILDER_RESULT_DIR}/BOOT-INF/classes /app/classes
COPY --chown=gitlab-proxy --from=builder ${BUILDER_RESULT_DIR}/META-INF /app/META-INF
WORKDIR /app
ENTRYPOINT ["java","-cp","/app/classes:/app/lib/*","com.example.gitlabproxy.GitlabProxyApplication"]