
# Gitlab Proxy
This project is a simple proxy for Gitlab API. It is a Spring Boot application that forwards requests to Gitlab API and returns the response to the client.

The main goal of this project is to provide quick responses to the client by caching the responses from Gitlab API. Refreshing the cache can be done by using a `refresh` flag in the request.

Currently, the proxy has only one endpoint `/groups` that returns the list of groups from Gitlab API.

Docker images can be built using the Dockerfile or using gradle. The proxy can be run using docker-compose or using docker.

The proxy can also be used to add more features like rate limiting, security, monitoring, etc.
It is also a simple project that can be used as a starting point to build more complex projects.
See the section [Further improvements](#further-improvements) for more details.

# Table of Contents
- [Gitlab Proxy](#gitlab-proxy)
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
    - [Add more error handling like retries, circuit breakers, etc](#add-more-error-handling-like-retries-circuit-breakers-etc)
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
[Edit](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1#R%3Cmxfile%3E%3Cdiagram%20id%3D%22sIVUU5Nqvf2JdoVW1s2s%22%20name%3D%22Page-1%22%3E7VxZc6M4EP41rso8OCXAYPvRR5KZ2Znd1GZrk%2BzLlAwyVgYjVsjX%2FvqVkMCcNomJnUychwS1RCOpu78%2BEGkZo%2Fn6hsJg9p04yGvpwFm3jHFL17u6xn8LwkYSLNESBJdiR5K0LeEO%2F4cUESjqAjsozAxkhHgMB1miTXwf2SxDg5SSVXbYlHjZpwbQRQXCnQ29IvUeO2wmqT29u6V%2FRtidxU%2FWrL7smcN4sFpJOIMOWaVIxlXLGFFCmLyar0fIE3sX74u877qiN5kYRT6rc8PX20mf%2FnP%2FA7Dgj%2BE9JtPv3762NSWNJfQWasVqtmwTbwElC99BggtoGcPVDDN0F0Bb9K64zDltxuYeb2n80oHhLBorGq4Hw1Dd51LoYD7VEfEI5TSf%2BJzDcIo9L0cqLkytdYkoQ%2BsUSS30BpE5YnTDh6jeRH2U1vVN2VxtRWh01ZBZWnymGgiV2rgJ6%2B3O8gu1ueUbTYZPX%2B78x47325jc38x%2F%2FP3UnbQ7hX2%2BufqrpVsef%2FJwQvmVy6K1X7t8v7lyF7uM6xBBakfz7w7Xre64ICrkcOVVTULZjLjEh97VljrMCnM75hshgZLaE2JsoywRLhjJCpgLhW4e1P1R41E0LnUzbo%2FX6d7xRrUqxcogdRHbsXeaIQeK1dWRfhtcgn5fz2iArgRLkQcZXmatu0zaivstwXy6CWtNzyqW1skpTEgW1EbqrrQ15hjpnSwjHYAsI7krOxjFA8l0GqLMmEhBk22ppbPjjjYwHxhs3zmzEf2CXeAu292T6Ncas4eURvHmY6prq1yisUlrmrzLzKjlHp3MoFWpkpVqrBTyLo0FRdUux98KTa6tpgeBUjzNNPp3wAjaMwQnXJwl8CQpUxKp4BSqXbD%2BXQg3NvyMvCVi2IZbUnwr34ebKmRTFL4Gyfjgh100z3IRIurDOWqe86dKloc44ga8aKebAzsDFNyoBkrcqAVeSWGTWOokkBRfP26BpgYiZd3ki73kfswx6rrTTk13%2BjxfOaAUblIDAuGnwgP90s6VNha0NhFxGvttxTymqWjW2VT2WcB%2BUwFNm8phMi1mETx%2FYognT4OiW40SieuF593yVPTiU3HAJU%2BaGcR%2BmLjLMIB%2BRmdiX2XzXWZtWyZqA7Fgd3LBN4evA2h6T16AT9Kr5fzbOuXf5AMSv3diozXy0XyZg9OOarW9EmiTuxdQVCoaETK0FVlIxid0Dr1og0DUt4QUQ%2F7XhqJmUjpkpdYmOjtAGhfwuKUj2uYis7HvFu8kNJhBX7FUUCCMCq1ZG3rY9WVPyK2MpbowF7GvngWCdaqHUc5uyp8QP0uVA4Q4okJO9kErQp3s7BJ2fEGTn5hzFGxDDlZt6DwtQpZnkR3HKPmJ2kr2xek5yCaUWznx8%2FObQPunGylv3kR0UVAQtpG%2BkGYCyq0JZGxJimcK59jbyKFc8HAuTEGJ%2FytiQyqMmPd9Jz7J90t2c94TSlNKuIYRiAue%2FctewEptdy8wVHICkfm21UNzupN%2FzB2iS2yjCqDYmTFEZlERMAuxZUFDynh%2FDUrp79jmqirgdShAhUfv3kB1zLHjRC6zDKWyONYEUMVlhhioQBGorBKcMl4Np%2FpnnDrj1IfDqRHnQonnIXqGqqpESH9jUGVoJVDF9YGnCH%2BiKUXh7BSJktDHa2Uu421xSHaouzRdtWOJt%2FguRT87sqbu26nDg0tN07SMNrS1hurw%2BXS7k1Of2nX4HKPkzdGeOnx1taPmhLtmTrMlx9cs8pcK9fmOfFcumPMK9V3HWwsMfkHvvyPgeVlgcGKfP%2FLE%2B%2BSP5l87%2FRyQnNq%2FmgUAid5sgx2vsi9%2B553RxAHDcxQWK0FvtFRZKb%2FabwSf4Tv1rOuMa1UHek4z%2F1Kn98I32JaVY5Qvih33DXbp69VzNe0d%2BqlzlnpglnpNORvkO%2BcctQJccy60czwPuuMQyDkGP8fg7y8GF2eFGoIPB03hQh6ieV0EyZz6knOIz%2FY2dXLGqqgqnAxizoHQ%2BwOLDxwIPSfcGQTBnkjno8Q1Rg52rOKx9%2BOiTo2DWjxOHYjvNLabmpJDdofypfEIAhSh1xJ1bifVshd0maB81QHhpO%2BQYvmu1HNvvr%2FnfJjZTNafP7eehMDPzfo7Zr58UC%2Frbyqj14rlpg%2BjUrVrSMfRKcvsX5rNaFWhllTzLUxjWlVyEP6cgf2SQdWvloHdYObByUeLdKycPysp4fReKdIp%2FVRKP%2BPHGT%2FeJX6MMd8LPFkwbp5c1d5jPadYwin5rq%2BALSUIVF3PyX%2Bt2SvijfZaqVUp4MQ141N9nNl90ceZlmmlQ2Xxoa5h7ImXo9YtophvnNCUo3%2B4WQ34zX8Hwpvb%2Fwggo9vtv1Uwrv4H%3C%2Fdiagram%3E%3C%2Fmxfile%3E)

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

## Add more error handling like retries, circuit breakers, etc
It can be done using a tool like Resilience4j, Hystrix, etc.
Resilience4j is a lightweight fault tolerance library inspired by Netflix Hystrix, but designed for functional programming. Resilience4j provides higher-order functions (decorators) to enhance any functional interface, lambda expression or method reference with a Circuit Breaker, Rate Limiter, Retry or Bulkhead. Bulkhead is a pattern used to prevent a single failing component from bringing down the entire system.

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