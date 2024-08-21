
# Gitlab Proxy
This project is a simple proxy for Gitlab API. It is a Spring Boot application that forwards requests to Gitlab API and returns the response to the client.

The main goal of this project is to provide quick responses to the client by caching the responses from Gitlab API. Refreshing the cache can be done by using a `refresh` flag in the request.

Currently, the proxy has only one endpoint `/groups` that returns the list of groups from Gitlab API.
Client layer is split into two parts: client and retriable client. The client is responsible for preparing the requests to Gitlab API and caching the responses. The retriable client is responsible for retrying the requests in case of errors. Retries are done using Spring Retry.

## Performance considerations
Proxy gets a full list of groups from Gitlab API and caches the response. It uses keyset pagination to get the list because the number of groups is more than 50000 and offset based pagination is not supported by Gitlab API for larger lists.

The whole cycle trough groups takes a long time. Approximate number of groups is above 600000. Given 1-2 sec per request of 100 groups results in several hours to get the full list.
The entire list is not needed for most of the use cases. More often use case is to get the list of groups filtered by a specific keyword. Moreover it should be paginated.
Currently, the proxy supports filtering by name. This is done by adding a query parameter to the request. But for this traversal of the entire list is needed, which results in O(n) time complexity.
This can be optimized by adding of an index. The solution is to add treeset and store the group entries in the cache individually using the group name as the key. This way the search can be done in O(log(n)) time.
## Modes of the proxy:
Proxy can work in three modes: normal, fallback, and bulkhead. It starts in bulkhead mode and switches to ready mode when the cache is fully populated.
### Ready
Client will constantly cycle trough the groups in background and update the treeset. The treeset will be used for giving the filtered and paginated results to the client. When an entry goes obsolete, it will not show up in the results and after configured time it will be removed from the treeset by special listener of eviction events.
In case of a refresh request, the direct request to Gitlab API will be made and the entries will be also updated.
### Fallback (to be implemented)
If an error occurs while accessing Gitlab API, proxy will return the last known state of the cache. After specified time if the service is still unavailable, the proxy will switch to bulkhead mode. This time should not exceed the eviction time minus the time elapsed since the start of last successful full refresh. Alternatively, evicted entries can be marked as stale and the proxy can continue to serve the stale data.
Refresh is not possible in this mode.
### Bulkhead (to be implemented)
If an error occurs while accessing Gitlab API and there is no previous successful full refresh, i.e. the cache is not fully populated, then the proxy switches to a bulkhead mode. In this mode proxy will pass the requests directly to Gitlab API, as this is the only way to get the full data.
The full cache is not used in this mode. But single pages and filtered request caching can be added to improve the performance.
In both fallback and bulkhead mode, the proxy will try to access Gitlab API in the background and switch back to normal mode when the service is available again.
Responsible should be notified about the error.

Docker images can be built using the Dockerfile or using gradle. The proxy can be run using docker-compose or using docker.

The proxy can also be used to add more features like rate limiting, security, monitoring, etc.
It is also a simple project that can be used as a starting point to build more complex projects.
See the section [Further improvements](#further-improvements) for more details.

# Table of Contents
- [Gitlab Proxy](#gitlab-proxy)
- [Next steps](#next-steps)
    - [Performance considerations](#performance-considerations)
    - [Modes of the proxy:](#modes-of-the-proxy)
        - [Normal](#normal)
        - [Fallback](#fallback)
        - [Bulkhead](#bulkhead)
- [Table of Contents](#table-of-contents)
- [Example of a target architecture](#example-of-a-target-architecture)
- [Build and run the application](#build-and-run-the-application)
    - [Build and run using docker-compose](#build-and-run-using-docker-compose)
    - [Build and run using docker](#build-and-run-using-docker)
        - [Build](#build)
            - [Build docker image using the Dockerfile](#build-docker-image-using-the-dockerfile)
            - [Build docker image with gradle](#build-docker-image-with-gradle)
        - [Run](#run)
- [Test the application](#test-the-application)
- [Debug](#debug)
    - [Debug building of the docker image](#debug-building-of-the-docker-image)
    - [Inspect built container](#inspect-built-container)
- [Further improvements](#further-improvements)
    - [Distributed cache scenario](#distributed-cache-scenario)
    - [Add more features like rate limiting, etc](#add-more-features-like-rate-limiting-etc)
    - [Add more endpoints to the proxy like /projects, /users, etc](#add-more-endpoints-to-the-proxy-like-projects-users-etc)
    - [Add more tests like integration tests, contract tests, etc](#add-more-tests-like-integration-tests-contract-tests-etc)
    - [Add more security like authentication, authorization, etc](#add-more-security-like-authentication-authorization-etc)
    - [Add more monitoring like metrics, alerts, etc](#add-more-monitoring-like-metrics-alerts-etc)
    - [Add more CI/CD like pipelines, deployments, etc](#add-more-cicd-like-pipelines-deployments-etc)
    - [Add more logging like structured logs, log aggregation, etc](#add-more-logging-like-structured-logs-log-aggregation-etc)
    - [Add more error handling like circuit breakers, rate limiters, etc](#add-more-error-handling-like-circuit-breakers-rate-limiters-etc)
    - [Add more performance improvements like async, batching, etc](#add-more-performance-improvements-like-async-batching-etc)
    - [Add more configurations like timeouts, retries, etc](#add-more-configurations-like-timeouts-retries-etc)
    - [Add more environments like dev, test, prod, etc](#add-more-environments-like-dev-test-prod-etc)
    - [Add more examples like how to use the proxy in a real project](#add-more-examples-like-how-to-use-the-proxy-in-a-real-project)
    - [Add more comments in the code](#add-more-comments-in-the-code)
    - [Add built-in support for OpenAPI, Swagger, etc](#add-built-in-support-for-openapi-swagger-etc)
- [Further features](#further-features)
    - [Further features on endpoint /groups](#further-features-on-endpoint-groups)
        - [Add pagination](#add-pagination)
        - [Add sorting](#add-sorting)
        - [Add filtering](#add-filtering)
        - [Add refresh (cache) flag](#add-refresh-cache-flag)

# Example of a target architecture:
![Diagram](docs/diagram/Diagram.drawio.png)
[Edit](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1#R%3Cmxfile%3E%3Cdiagram%20id%3D%22sIVUU5Nqvf2JdoVW1s2s%22%20name%3D%22Page-1%22%3E7Vxbc9o4FP41zCQPZHzDhscEkrTZZjez6W6avnQEFsaJsVhZEOivX8mWwbLlC8Qm0NKHxrodyeccfecimZbeny5vMZhN7pENvZam2MuWPmhpmqWp9H9WsYoqTFZiFQ527ahK3VQ8uj8hr1R47dy1YSB0JAh5xJ2JlSPk%2B3BEhDqAMXoTu42RJ846Aw7MVDyOgJetfXJtMolqu5q1qf8EXWcSz6yavahlCuLO%2FE2CCbDRW6JKv27pfYwQiZ6myz70GO9ivkTjbnJan%2F1%2F%2Fv5P%2F9H%2F%2BdW6v8N3A9B5%2Fd7uRFMtgDfn6wcYgoAvgKzit4oXj6FPdp5M1TOzZSZyMJrPeDeICVzKhAaGcXclu7D89%2BRLvYVoCgle0XGcumFyOlznVJ2X3zYSVBVeNxGkxysB1xpnTXvDHPrA%2BbMFr3rlrKKc8m3IiCgt%2Fept4hL4OAMj1vpGtxatm5ApnXSg0sex63l95CFMyz7yaacrGwSTcDhrz2V5KWsjqWZ5%2B3G8UzUJ80yPzns1w1BgovnfnG2nqxF9S9IeRQy6pF2wMzyj66MLUOI%2F5yGblEqdxsgnbT4N6%2BkjPAVeom0BsAvo3xFguCTt8saZxRoNRYlaPEgIxO2ACtr1nexIhGcT4HOSYE5QVE2lStouVRefE1Rmy0QLwXTMmJKJCYYKEgkxRESR2hvCtriENTm66uGrSykysgFF5zawX%2BYBSZMQ%2BxGMXmGba0x2eTYcIQyIi%2Fz0%2BsJm4LkObwkIwCRqGoLRqxPukbRctU4nklbyISm4MZi63ioaQHUETNlm4ppyB8kVBi5lsabcIx%2Bl2yOCU9oSRJtxow6hrWI0exfdWdTXXI8yHf431FM61pcqamZ9n6C3gMQdAelMqiYwMsGpEVUFiLk8GXa0%2BXrTGlXERzVm3%2BbhXPpeDiS3DNmD%2BAXpxo7eUXzvIU7X0I7hpo1rUyDIXktEukiZYqyz4RjMQ0ISBOTsGKx5wfCPstK75A1T17bZTFJ0FfG3YTTlZCSGyZJgq9YYtGYdhvfZpd05VWzSzZRJt2QWXWKUuk1xTpMZpUNwfnQl5fwoElbpMl51muKVeTTOT7SyYudnv7yLl76F83OQzorcqP%2Bqfsxuzkp1V%2FTQPRoZpUK3JD3NI8QLdwT37V5wCTbsW%2Bwfn%2FTG4Mk4UDNodCqYQW2vUN49HjNolKvZfnmnyXJNpxzAr2g7f7UcQN9zmYh%2BEwO1I3LUYaDUDDezi94lzW3tw8SphdZsHdzGpyhZY6ZLEwd1APLdw7CHvz%2F9UMjsr6snF43vv9zJk7LvsWaC6XI8EAR8nIOBzXZQ%2BRaYogXnMaOBId2dyTIigCTKNqTAmyhD200WPTR6Xa9dlODGuJbnf6wcrciKeiXuhFLR1pFvl4v2FHEen9X8jSPObeLKy9msJKY8IgNdJ%2FrErZ0s%2FJgN2Wv5kitESZG1rRrt7BxgRtzLZ5Voko1qqXy111iIpDRpkXd3CSOJbnMEIs3kN8Y3VXY2f7J3J3t3qPaungxrn1LByPMgPoAk6%2BZY9zDC2DzMWokAVYb1TUGWJCI1lD4YTeDDnLwP9N%2FBs2JzqaUvZUkMZlcW59TARHT18vnRfza8Pwbo6Xb6498Xa9iWZa4j%2FbbdxUa5k1qfbNBvqAKHKjpoWVfLljXIMB7aDnzkRYTJBDnIB971pjalr5s%2BXxCacYG8UJOw4vciOYomxEVFglff%2BPiw8MwKF524OFgmGwerluR6A1y65FviOSJhGgYvb2iwwipReIDYpdIIuSD3S%2BM7mwA7kBRIQ%2BXiYDwTRgZojkewQO9yMk5c7drKha5bhqB668kw9KhxWYgTyhSNk39ALsvkrT1ALaXS3Y5IIlo7H5XMO6UIrZ2gmJDVu0iRihhYQCruiMbjAAp9wt2x5kylDTMw1MvONwLaj%2Fakjz%2B7juIsYtT5KAXf6PSzoNIVFLySWspzIapULbPq9k5VMnodQQMyibscVdpBtvLtJwkeuEkJA6cabIqE%2FdXjUsNK7RBJBlRmPeqIU%2BUMMz9kN0iRulOM03WYiFwRJuG5ENjLLYDSyFa7xBisEh1mbAsFBTvRkIN6HnZn%2BludlHJFK6h3u2adl9gbucz67WG%2B5IJy%2F2bueQ%2BATM7Os50uRjQgYEHNWWkAUnSyqmpdIZpKhx3LnGjj%2FMMhRlfLIUaVpSbqOGORSlnPz8SHnmh5bKiqPJzOBpn5fi2GYwwD9sYEz7PQvweYY29xwwPwQfJGNGvgo9S4YxxDttjVR%2FYv497uCIBysO1a3Wbc4qJIrwGv2OxaolMcO7M1O8UZE1zVKTbKCOW4xNsCvmrK56kK%2BFv3V4X%2BooFoyJ%2BXwkv27vft9dcw3XXjCJf7N0bi7E%2FaGF6lUgjV8CCL2ofuBFHNV1Mw0NXLgICVKu%2Fpco9I7uhUQAO1%2BM6bcqH1DK2WXdxJ%2B9u7hrZmjntUPbCNISv1DrXsi8KY73TX7HTX7NjumrGA%2Fag%2FtYrWEH9qXVf2IG3iJZ8PNfXdlRxiZGe1J4g5QczhQ8ytSzwwPPLbMlsjiJnK0Fe87VFH%2BlGai9dO%2BHHCj6PEj4FLeeEO54RuT6pqx%2BiwZH2UarnyLRyWVO5Dl92SaupenhRw4qDow07%2Fuoopxu2KvlPcvv3BYBEAN30u2DF6oufa3LmgPOMgzxFlbwatU0YfqSStrfK7OVmYkpROfra18hlXUVrWUhVVkHhTedn1%2FZut87Lpizzpm5nvuKrw7sM6Y7tcbN2HdfI9VOH3D5r%2BgCgt%2FYr%2Ba3MfmciueZ8u3R64L3q6dPvOS7c3mJKBvn0AV24%2F8KsUMwfosui1h%2FC6yJ4fwM8iRcwq%2FXoi%2F%2BPBun4WiRY3v1UZmcbND37q1%2F8D%3C%2Fdiagram%3E%3C%2Fmxfile%3E)

# Build and run the application

## Build and run using docker-compose:
```bash
docker compose up --build
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
# Further improvements
## Distributed cache scenario
The proxy can be used in a distributed cache scenario. For example, the proxy can be used to cache the responses from Gitlab API in a Redis cluster. It can be done using a tool like Redisson, etc.
Alternatively distributed synchronization can be achieved using Terracotta, which provides clustering capabilities for Ehcache.

## Add more features like rate limiting, etc
Rate limiting can be used to prevent abuse of the proxy. For example, the proxy can limit the number of requests per second, per minute, per user, per IP, etc. It can be done using a tool like RateLimiter, etc.

## Add more endpoints to the proxy like /projects, /users, etc

## Add more tests like integration tests, contract tests, etc

## Add more security like authentication, authorization, etc
It can be done using a tool like Spring Security, OAuth, etc.

## Add more monitoring like metrics, alerts, etc
It can be done using a tool like Prometheus, Grafana, etc.
For example, the proxy can expose metrics like the number of requests, the response time, the number of errors, etc. It can be done using a tool like Micrometer, etc.

## Add more CI/CD like pipelines, deployments, etc
It can be done using a tool like Jenkins, Gitlab CI, Github Actions, etc.

## Add more logging like structured logs, log aggregation, etc.
It can be done using a tool like Logback, Log4j, etc.
Structured logs is important because it makes it easier to search, filter, etc.
Further this logs can be sent to a log aggregator like ELK, Splunk, etc.
Errors can be aggregated using a tool like Sentry, Rollbar, etc.

## Add more error handling like circuit breakers, rate limiters, etc
It can be done using a tool like Resilience4j, Hystrix, etc.
Resilience4j is a lightweight fault tolerance library inspired by Netflix Hystrix, but designed for functional programming. Resilience4j provides higher-order functions (decorators) to enhance any functional interface, lambda expression or method reference with a Circuit Breaker, Rate Limiter or Bulkhead. Bulkhead is a pattern used to prevent a single failing component from bringing down the entire system.

## Add more performance improvements like async, batching, etc
It can be done using a tool like Reactor, RxJava, etc.
Reactor is a fourth-generation Reactive library for building non-blocking applications on the JVM based on the Reactive Streams Specification. It could be used to improve the performance of the proxy by making the requests to Gitlab API asynchronous.
Batch requests can be used to reduce the number of requests to Gitlab API. For example, instead of making 10 requests to Gitlab API to get the details of 10 groups, the proxy can make a single request to Gitlab API to get the details of 10 groups.

## Add more configurations like timeouts, retries, etc
It can be done using a tool like Spring Cloud Config, Consul, etc.
Spring Cloud Config provides server and client-side support for externalized configuration in a distributed system. With the Config Server, you have a central place to manage external properties for applications across all environments. The concepts on both client and server map identically to the Spring Environment and PropertySource abstractions, so they fit very well with Spring applications but can be used with any application.
Another option is to use Consul. Consul is a distributed, highly available, and data center-aware solution to connect and configure applications across dynamic, distributed infrastructure. Consul provides a flexible solution for service discovery, configuration, and segmentation that can be used with any application.

## Add more environments like dev, test, prod, etc
It can be done using a tool like Docker, Kubernetes, etc. Another option is to use Spring Profiles.

## Add more examples like how to use the proxy in a real project
For example, how to use the proxy in a frontend application, how to use the proxy in a backend application, etc.

## Add more comments in the code
Is always good to have comments in the code to explain why the code is there, what the code is doing, etc.

## Add built-in support for OpenAPI, Swagger, etc
It can be done using a tool like Springdoc, Swagger, etc. It is important to have a documentation of the API to make it easier to use the API.

# Further features
## Further features on endpoint /groups
### Add pagination
Pagination can be used to limit the number of groups returned by the proxy. For example, the proxy can return the first 10 groups, the next 10 groups, etc. It can be done using a tool like Pageable, etc.
### Add sorting
Sorting can be used to sort the groups returned by the proxy. For example, the proxy can sort the groups by name, by id, etc. It can be done using a tool like Sort, etc.
### Add filtering
Filtering can be used to filter the groups returned by the proxy. For example, the proxy can return only the groups that contain a specific word in the name, etc. It can be done using a tool like Specification, etc.