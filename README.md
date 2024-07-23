Build docker image option 1:
```bash
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=gitlab-proxy
```

Build docker image option 2 (uses the Dockerfile):
```bash
docker buildx build --platform amd64 -t gitlab-proxy .
```

To start:
```bash
docker run -p 8080:8080 --platform amd64 -ti gitlab-proxy
```
