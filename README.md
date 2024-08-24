
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
### Bulkhead
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
[Edit](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1#R%3Cmxfile%3E%3Cdiagram%20id%3D%22sIVUU5Nqvf2JdoVW1s2s%22%20name%3D%22Page-1%22%3E7Vxbd9q4Fv41rNU%2B4OX75ZFc207bkzXpmbTz0iWwADcGMbIgSX%2F92bJlkCxjTDAkPdMXx7oL6dvfvkhOzzmfPV5TtJh%2BIjFOe7YZP%2Faci55tW5EHT57xJDJckTGhSSyyNhm3yU8sMk2Ru0xinCkVGSEpSxZq5ojM53jElDxEKXlQq41Jqo66QBOsZdyOUKrn3iUxmxa5oR1s8t%2FhZDItR7b8qCiZobKy%2BCXZFMXkQcpyLnvOOSWEFW%2Bzx3Oc8rUr16Vod7WlVEwsY0%2FlVMsZUTxnbXr4Nv%2Fvn%2F84389%2Ffgk%2BfaAfLpB3%2F3c%2FLHpZoXQpuj1PE95hdTRKlvMY867MnnP2ME0Yvl2gES99ACRA3pTNUkhZ8DpO0vScpIRCek7mUOksRtk0b87LV5iyBBZ9kCaTOeQNCWNkBgUpGuL0hmQJSwgvGMFUMJVafKxUYIQPjUQ%2F6%2BpjMmcCXBas%2FBnAhaFkzsvy%2BfPyKzRLUg7TdzhdYd47FOiLKhaeTwA%2FSllika8xmWFGn6CKKHWC0HAB0J7tO5YXCHys5cExzMCPbKd8FsUPG7BBIyMIIzdw%2FDDvp6gxlWBnu5YRRG4YWMUzFHWQQP9kPakNHuBFQKIeHh9uhhH9%2B%2B67yRb%2FObtLyPjTxw99y9bwMVgsDgOHgoRJirJMtJtQFHPsVZBTA6YZWaFhPjjvg%2BIs%2BSmnCUNMSgNFYTmN40ROpmR0v5674BWpeBeUuoLsVpk4JlYtuxZrJVgty%2FCc0PWt8qmB1QOwS81tXwerA2OEUeA6XvGMgsPBWstlvobVW0xXCcDwqGT2bK45OnI6AAgoFUMQWQETV0VI4BobdMDT1RBiOb4R6nwoIwRyDZkSOwDI6v7mazJ4z%2B4eyTW%2B%2FOPz5H550Xc1gPzJ%2B82F%2FagQOQlHvJS%2Bc0PH8KUdDhWEOKZpyPou1AASuYbEH45VQyGBY3gKhRwOkAvXGnhfGerfxtNz%2Bj6ZmJNVP9BggGMwD0WSUDYlEzJH6eUm90wFyqbOR8L3MN%2F%2BH5ixJ7E9aMmICh5Yefr0lbc3vDL5TXSXJy4eldRTmXpMmNQMUt%2FKHuF904gnniQcyDjZHxcZWdIRbhCy0pJniE4wa7ItRI98iRtxRnGKWLJSLfS6vRdNb0jCDdgSnx4Q0Ia%2FQtdR8Rm6xtqOgmcFWsXPFT3KxnV1ED8wZD1qK4O4ZrMKLNZKG2VAKXqSqi14hUxD%2BXqpWgGfnP14fzv%2F5qZ%2FXJC769n3v34Ew76nMeP15ZcedyiuJgBwcL5sP4XNPBtSRT78f5bcn8lx1M9yYA2ggmUvHnMQleXwNuF%2F33yGHi3T5PhIZjh7%2B6IC1zcNR5E4I%2BIoapY6nrrBFGbP1vRaiqIlCaJpcK%2BxSRh5otrTCwlorT1leS0FVMiAaVihaan2gaD7A0W4WcWAhBuSjRo6FelqLcNNjlt1ELficm0R4c0gZUUyHmdYqfMMIW4kVEmKS7GNk1Wt3I4AP6w%2FKkwXLrl0MnwD%2BIAJmOWftznazFaVCh4ohuE154TOUCqVrRBNEPwdIR7Qqa3yIBQ%2FL3RNsygBDw6EpJ%2BBCZbMJ3pLsFVYPwFemIuWpmCgooRRNM%2FGUL9smdto3A4hNFZ7XTeEiQzvE2jLO8gYJfe4L8wWfYAYjwhFhfWmjpAXC0tukG8CogUlmkM0up%2FkdFbdAtvzioWVX5Q13s21l9NzNJriEgIAqRwFRaHGu3yaKk0WP7k0a2M8Rsu8oxpjd6ehOkviOCfvOkNaJXTFcC7mUEbiDvK2OrB0vSgyZF9X9YW2KXnJ1A1UGgtC3dS1LQP22net8ukfydbVIzu%2FyeLfSxYXCUw6GS4ZiB5s369IHDpXdBOm6YA4GkPCrhcqloWn0YZjqx5MnYvsVEJ17pFow9IPIk5gsis7%2Bcod3%2F8Pf9ZVo3aqxe36sPbbddTe7mz9HPzKD1V9aifyDB3wz%2Fs9XfZVvzYbkStW4bmGf72jVu%2B9d%2BKxaxGAbdZB6w6dK1AEhcf7uE3HdDSU7aNZHkjNnxSPKc44pzK6xJ2tTysteYqwRm0QAlTPCwQh2oYM%2BiArZqnODuVF29iIJFBG5aQiMg1TUrPP5MVd5yF8FHMj%2FF67QN%2BRogSNako%2B8gdzj5u0pbjkgL7mUr%2FOqkf170OS1q5jqJ6TBipsfNewZLdPP0ZrcUoCRYbiOoaH24D1ENIVDqD6MzgCN%2Fx6jcZ983jAr%2BbwTeLH%2FclIBUbVjAeXR2xTTl3zWEqNlnSlGYIq43nNfFcGf9eRYKlVQ9y3vafSralZq%2FAFeckR3KYDz51sLB%2FomzqwyryDCVq9bKBSpx1ExiZ4EkYV8HYTxLUDw1Iuzrw%2BetbjMmNCL4GhO7OKXDMPDN4sWVvjZS8BruL%2FZAINmtdWhVqcwOx7nHME59BtKbGtncj1mYtpWjXXcn5L8ctKsX7VpCvxxTkVmD7fXVjNrF6Gha9zLVy3U2tk81kCvJ9Gfhk921Tv19CzkWolOseQUKfxQtcrEFBbvyyoS4nkswuHRQH7wR77fpqySdGBO%2FsMPddKMGrvi1id67NDMR%2BZKgJLWdqBua7w5OhedWln1V4v5OuR%2B7MqItp7GfKlZ76XIpgKnXtnPe%2BiBl36bjdLRtW3XX90IUbtyd81bAnumKGjMkMZq3zudnfKDI3Qlg1wEa8cdBQzzAOqBsDxapmmN4hN37ztqGdDxDSyN2WH2QLNa7tsOs21eOhAP6rdPb4c0C1GLvL1e177hY5eMHLDT9a2hxULC1zTdPL9592fc3i2cmJQDRp2dkE%2B0pB9C%2FwUL1N%2B6HvM0J7%2BfU6aLP4qNw%2Fe34mKp%2Fywo%2BuPkF70gLkSXvR9BaW7L2GHrmEr0XMdpUe4hN147VA%2BzkoYbMphGG0fJ%2F4F9z%2Bo3ExSj0pbhJetyrFG6OgAsMLI2DAdPDv46qwWAboGvqKweGAOH1eN%2FNKfcrmW4Sl6pvIt1%2B4PD18VBuyaYypYPUrSFNb4IBj8e77esvKvEio4MGs3vmavbcc1nA6uH9a7GY7uMJ0y7rz7QL6MZfXzGKsS0DKjqDmkVXOovnU7d0ahxBa8kuiSXW5UaQFbFQpofZReddntdi77vteIquNYkfmseXV1hadeHEId%2FSe8MmKYYBPICHfd%2Fe7T7RC2GnmQw1nF4Jtwlhi9s3jWzjCV3fak9FDh2YX5LcJzOOajE2BYt9u%2BUIxveZxGvllSRj1Oebnktwd6bPPTrSCOfxlua6ZG7mfW6CZ%2B%2F3JvS6PH%2FYLy%2F6kUKN78Uxrn8n8%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E)

# Build and run the application

## Run using docker-compose:
It will be run from ghcr.io image.
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

# Links
https://docs.gitlab.com/ee/api/groups.html