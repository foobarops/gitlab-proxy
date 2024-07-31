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

#### Build docker image with gradle:
```bash
gradle bootBuildImage --imageName=gitlab-proxy
```

### Run:
```bash
docker run -p 8080:8080 --platform amd64 -ti gitlab-proxy
```

# Test the application
```bash
curl http://localhost:8080/groups
```

# Debug
## Debug building of the docker image:
```bash
docker buildx build --platform amd64 --progress=plain -t gitlab-proxy --no-cache .
```

## Inspect built container:
```bash
docker run -p 8080:8080 --platform amd64 -ti --entrypoint /bin/sh gitlab-proxy
```

# Diagram
![Diagram](docs/diagram/Diagram.drawio.png)
[Edit](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1#R%3Cmxfile%3E%3Cdiagram%20id%3D%22sIVUU5Nqvf2JdoVW1s2s%22%20name%3D%22Page-1%22%3E7VtZc9s2EP41mnEe5OGh89GSbKdp0mbiTh33JQOREEUbIlgQsqT%2B%2Bi4I8AJJSbYZ2bHkh4RYgItjd789QLXs8WJ9zVA4%2F0JdTFqW4a5b9qRlWX3Thn8FYaMI%2Fb4keMx3JcnMCDf%2Bf1gRDUVd%2Bi6OCgM5pYT7YZHo0CDADi%2FQEGN0VRw2o6Q4a4g8XCLcOIiUqbe%2By%2BeSOrD6Gf0j9r15MrPZG8qeBUoGq51Ec%2BTSVY5kX7bsMaOUy6fFeoyJOLvkXOR7VzW96cIYDvg%2BL3z6Oh2yf25%2FGDz8c3Tr09mXz5%2FapiXZPCKyVDtWq%2BWb5AgYXQYuFlyMlj1azX2Ob0LkiN4VyBxoc74g0DLh0UXRPB4rGh5BUaTe8xhyfVjqmBLKgBbQADiMZj4hGkmtCDOO17V7NdMTBM3DdIE528AQ9UKqPkrrhl3ZXGUitPtqyDwvPksNREptvJR1drLwoA63%2BqDp6P63m%2BCuQ36f0NvrxY%2B%2F7%2FvTdqd0zteXf7WsHoGZR1MGTx6P937lwXmDcpe77KsII%2BbE6%2B%2BP1q3%2BpCQq7ILyqiZlfE49GiBymVFHRWFmYz5TGiqp3WPON8oS0ZLTooBBAmzzXb0fN%2B5E4xxOTrUn63zvZKNaNTIEe0bMw3zL2SUYIna3j%2FTbxrkxHFoFDUgEyzBB3H8sWneVtBX3r9SH5aasTauoWGZHU5iILpmD1Vt5a9QYWZ0iI8swiozkqWxhlAyks1mEC2NiBU2P5fk6mxhRHhw6xhg5c4ymoE0V2ispMxqvcCZBAqj%2FLgXKjT5i8oi576CMlLwKe72uU3xFgT1Ixi%2Be7Kx5lssIswAtcPOcP9SyfAlO11pjCXdrzazT12zBNkooaxoVKNvTVL0xkE1d7WERce3z77lniYdd1crQUDQSMKxD0WeDqMSdPUB0N9p29kTbp0HpBWNokxsQChiLfgps2Q3HNA3YimXvtpXuIU3F7J1MZZcF7DYVo2lTeZlMy0EmhNccQ2x9UXarcZx5tSTkK2QqZx%2FKA84hp%2BLID6LUXUYhCgo6k%2FgqB06Ztx0Zx1%2BIDXvTMzgc2IdhWgP5YHyQXk3zb%2Bucf5MTpH7vlY3W1oO9KgdnHtRqBxXQJk8vZLhSNCJkaCuykExA2QKR%2BICMuO8RMR%2FB%2Fw4SKXXlkJXam%2BjsGNK4DAKWjlkbROb4gVd%2Bk7JwjgLFUkGBMCq85m1EfC%2BQPRFYGc91%2BSDiQM1lhOtcD2fAbgYzJHOpbFGII87zixOtKHOLq0vZwYamDz5wFGwjAKs2cu%2BXEddZFMdxRh9wW8m%2BvDwXO5SBldNAX98UOQ9erLy6iVjdrrSN%2FIM0E6PamoyCLUnxzNDCJxs5FASPFsIUlPg%2FYT5iwoih7wsNqN4v2S2gJ5KmlHKNYhAXPIfng5BX2u5OYKjlZMTm21aTarqjT3OD2aPv4Bqg2JoxxGZREzALsRVBQ8p4d4lC6e%2FEAVUV8DoSoALRO7lQHQvfdWOXWYVSRRxrAqiSLDQBKqMMVL0KnLJ%2FGk4NTzh1wqmjw6kxcGGUEMxOUFWXCFlvDKpsswKqQB8gRfiGZwxH89dIlIQ%2BXilzmWTFIdmh3jIt1U4k3oJTiv%2B2ZE39t1OmNc5N%2BCtoQ9tsqEyrp9sdTX32LtNqjNKLhR1l2vpqx54L7nc1zZYcD14Dfroj35YLal5hf9fx1gKDd%2Bj9twQ8zwsMXtnnj4m4bjw2%2F9oZakDy2v7VGryG%2F0wKjVlx8S7XU11o3Mvn2lt8bq34dlccGy8kVruZrlbRsmzt%2BrLGnTXlTbolbxLfghtbrr3P%2FoDOWIsN7i9wVC4LvtG6da02aELeWz22BVJWMY5KxPxSddFv%2BAbPvO3u9TRGeoX0sLfdlZ%2FCnEqrv2DQcipZvLBkccWADQ7c91yweNGnZFo81TlcOFX9wV5VueKUkL1HbHtvCdk37PrRscGHno%2B9PoCcwpxfDwqOOMx5SjBzEYY74phjgR1bS7h65Q%2FgD4s6e3yTB1HohfjFRnaoOTkUT0i%2FBYkhQBEGLXGl4eZazpI9pr9JyC5Gsi%2FI7vJ9W%2B9FSrn7jouS%2BkyzLLodnwJ2m8np9S%2FY0wD3qTl9p6sXB%2FbL6ZvK181yMel4VaquQnQYnep1h%2BfdZrSqVCna88KtMa2q%2BM3DKb96l0HVe8uvrn1O0PTYIp2e5s8q8qtBM5EONLNfa0q8yX7yal%2F%2BDw%3D%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E)
