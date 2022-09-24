Build docker image option 1 (doesn't use the Dockerfile):
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=springio/gs-spring-boot-docker

Build docker image option 2 (may not work due version mismatch):
./mvnw package
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
docker build -t example/gitlab-proxy .

To start:
docker run -p 8080:8080 example/gitlab-proxy
