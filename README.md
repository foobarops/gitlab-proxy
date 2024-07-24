# Build and run the application

## Build and run using docker-compose:
```bash
docker compose up
```
## Build and run using docker:

### Build

#### Build docker image using the Dockerfile:
```bash
docker buildx build --platform amd64 -t gitlab-proxy .
```

#### Build docker image with maven:
```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=gitlab-proxy
```

### Run:
```bash
docker run -p 8080:8080 --platform amd64 -ti gitlab-proxy
```

# Test the application
```bash
curl http://localhost:8080/groups
```