FROM eclipse-temurin:11-jdk-alpine
RUN addgroup -S gitlab-proxy && adduser -S gitlab-proxy -G gitlab-proxy
USER gitlab-proxy:gitlab-proxy
ARG TMP=/tmp/build
COPY --chown=gitlab-proxy . ${TMP}
WORKDIR /app
WORKDIR ${TMP}
ARG DEPENDENCY=target/dependency
RUN rm -rf target; \
    ./mvnw package && \
    mkdir -p ${DEPENDENCY} && \
    cd ${DEPENDENCY} && \
    jar -xf ../*.jar
RUN pwd && cd ${DEPENDENCY} && ls -la * && \
    ls BOOT-INF/lib / && \
    mv BOOT-INF/lib /app/lib && \
    mv META-INF /app/META-INF && \
    mv BOOT-INF/classes /app && \
    rm -rf ${TMP}
WORKDIR /app
ENTRYPOINT ["java","-cp","/app/classes:/app/lib/*","com.example.gitlabproxy.GitlabProxyApplication"]