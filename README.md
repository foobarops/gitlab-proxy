Build docker image using the Dockerfile:
```bash
docker buildx build --platform amd64 -t gitlab-proxy .
```

To start:
```bash
docker run -p 8080:8080 --platform amd64 -ti gitlab-proxy
```
