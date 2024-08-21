
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
[Edit](https://viewer.diagrams.net/?tags=%7B%7D&lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1#R%3Cmxfile%3E%3Cdiagram%20id%3D%22sIVUU5Nqvf2JdoVW1s2s%22%20name%3D%22Page-1%22%3E7Vxbc9o4FP41zLQPeCTf%2FZh72227mU130%2B5Lx2ABbgxihSBJf%2F0e2TJIljE3Q9NpZzIG3YX0nftxOs7F%2BOmGxdPRB5qQrGOj5KnjXHZsG2Pb7og%2FlDwXNRF2i4ohSxPZaVVxl34nshLJ2nmakJnWkVOa8XSqV%2FbpZEL6XKuLGaOPercBzfRVp%2FGQGBV3%2FTgza%2B%2FThI%2BK2tAOVvVvSDoclStjPypaxnHZWf6S2ShO6KNS5Vx1nAtGKS%2B%2BjZ8uSCYOrzyXYtz1mla5sRl%2FLrda7oiRCd9mhi%2BTv%2F%2F6z%2Fl68f1T8OEde3cZew%2F%2FdsNilkWczeW0F1kqJqyuxuh8khAxFeo454%2BjlJO7adwXrY8ABagb8XEGJQxfB2mWXdCMMihP6AQ6nSfxbJQPF%2B0LwngKh36WpcMJ1PUo53QMDVncI9ktnaU8paKhD1shTBnxvtKBU7F0LOdZdh%2FQCZfgwnDy5wAXHqcT0ZbvX7Rfx%2BM0EzB9Q7IFEbNDg3mo8uDFBsiTUiUP%2BYbQMeHsGbrIVicILRcA7dm%2Bg71A4kPSA3YdCwV%2BZDvls2h%2BXIENBllBGLmB44f5PEWPkQI728VWELlhgItnKPvEEv3D5aZWeIAvEhL18Hh324vYv%2FdfEZ%2F%2BeX6f0sGH9%2B%2B62DbwcTadHgYODQnDLJ7N5LghixOBvQpyasA0pou4ly8u5mBkln5Xy5THXCkDjyJqmSSpWsxo%2F2G5d8lXlOZNUGoLsmtp4phYxXYt1kqwYmx5Tuj6uHwaYPUA7Mpw2zfB6sAaYRS4jlc8o%2BBwsNbyMt%2FA6h1hixRgeFRmtjevOTpyWgAICBVLMrICJq6OkMC1VuiAp2sgBDu%2BFZr8UEUI1FoqS2wBIIuH28%2Fp2Vt%2B%2F0RvyNUfH4cP88uuawDkLzFvTuxHhchJeMQB8s7AQw1q1kLEDR3LV2441BDiIGSp8i40ABK5lsI%2FHFzDQgLH8jQWcjhALl185n3mcfcuGV2wt%2BkQDRfdwIABSUA9lEXK%2BIgO6STOrla15zpQVn3eU3GH%2BfV%2FI5w%2Fy%2BuJ55zq4IFjZs%2BfxXjLK4tf5HR54fJJKz3LkoYw8pRyZQ4ofVFaVjOIwrMCChU0uwsaHrMh4U36g%2BwojlHXY%2Bmc9UkDrWJUDzpGspinC322OiDIobc0FdpsCVYPuNGKmYWuo4M1dK2lUgXPCs6KTcsZVU27uogfWKpQtbVFXNQsD4tDNVY5Yyx%2BVrpNRYeZAfnlUW1FBfT829u7yRc3%2B%2BOS3t%2BMv%2F7zLeh1PYNN3lx96gjr4noIaAdLzPYzuPXzHtOIxf9vLoybHEfdWQ6sM%2BiA7elTDqKyHb4NxeerjzAjRkgAKR2T2esfSn1dZDka%2BVmRQFEjCealW8Jg93zJa0tSxAohIkuYkE3EKArVmQ4n0C3JTKfkWuUKe7WUvFYqIAuHCOvKguT9B5Jws7wBCrcUhTV0KtS1NQ03WXHVRdyK%2FbWGhFeLlB3pYDAjWp89iLiR8ypUXJJtki5q6bYP%2BOHdfqHHCMplw94rwAdsAJUfr3O0oa06FXygWEb0nFA2jjOlbRGzNIbPfiy8O7VdHqUWIBpdhIoWMOeASLoz0MfSydAcCboM76bAFyZyJJIcqGjhLJ7MBtC%2FHJkrbEIpoSzRZ10OhI30HlIYKyaYcUYfSFfqMOYCCelTFheqnL5C3izVurP8EmJWsETUi%2FsPw5ydVa%2FA9rziYNUv2hlv5rUXcX9ESgAAoHIMFE0G1xWb1Jlk8YNLDTchg3ieT1Sj927UWcdpkuSsu06n1tm5puEUeyidcgcZXi3YRV4UWarZq5tF60S8ovUGOhMLQlPrtbEFN%2B27uHz6R1J7TSfPb1bx67KKyxQ2nfbmHEgPru9nZBwmr2jHY9MC42j0DrteqOkVnsE2HFu3X%2BqsZafitXOPxDawGZM4gcL%2BAszeX8yadXUHnq5vuz6c%2FXoZtbMxW78Hv%2FJDdYvaiTzLBPx%2Bv6fNuerPZkVyxSnsq%2FbXm2n1tnsr9rph%2F6%2FTDrae0LkGQVDYu0%2FrZExLS9l%2BPM59qvmTkQEjM8FTOZuT1s5nKyl5CqdGrQsCRM8PcEFs6zDoAq2gUpwdyhdta0WSwDIqQYsIWUgRs3vyxU2hEbEKWhG%2Ft52b70g%2Bgkbnjxr9B3VPqLQlueSAvhFUv6yqR%2FXveMm28RIv1EOmgQ4b37WwavaZEbUtAibQZGmmY3i4DlgPIVPgAKo%2FgiFwKzJtDN43Sc5Elo64JBH5T%2Fs6MKpqPJg88ppy1jVJlFJ%2FzhaGIqhzPK%2BZ35Wu36UfWBnV4PXd3lLZkXsaeuUGj26tBiC5merQXR8MNUGqxvaRCayy7mAGrecd6KzTDiJr5TwJowp423Hh2oGFtRyal8eeTb%2FMgLIr4NCtaUUuyh2Dt3O%2BrfKyEwFX8X8yggbJa%2BtELeMvuwZzWqbYprSEjRS7zohcRlwQwjUZOr%2Bp%2BMdSsZl10hb5kpwVIF%2FcLpzmrJ6Gpa1zI023U0tktBcB7yaRX4icXd%2FvhcrZSNcSnWNQqNOY2%2FUCCNQ28wZNKlFsdmmwaGA%2F2GLfTVI2CTowZ%2FeQc%2FsRRm36CD5Unh2K%2BQjpCCxpaQPm2sKTY1rVpZ5Vm2koDj23Z3VEbG9lqPnP4i6lMxUm98473mUNujrrrFg9%2FbmzfJlBxUEDETU5d1Do6Jyh9FXue92tcoZGJKsKuPRXnrXkM8wdqhbA8XqeZbcxH7163dLMlvRpzF6VE86m8aR2yqZoLhauAzNUu3l91aFbrFzUm1leu7mOfmDsTkTW1rsVCw3ckHRqKvTmNzs8W4sYVJ2GreXKRway74A%2FJfNMBH2P6dozX9XJ0uk%2F5eXB9zey4ynf8Wj7faQfGmCuuBd9X0Pp5nzs0LVszXtuovQI%2BdiNSYdqOCvlcCmHYXR7P%2FFPeP9BJTNJD5Vu4V7GlbBG6JgAwGFkrTgdPFt4Aa0WAaYEvmZweKAOH1eM%2FNRvdbnY8jQ5U3mta%2FM7iC8KA3ZNmApOj9EsgzM%2BCAa%2FzotcOH8noYIDVHvxNXdtO67ltJB%2BWG87OKbBdEq%2F8%2BaAfOnL6uY%2BVs2hhaKo2aVVE1TvtOeFWmN2nca7ZJcXVWrAuMICtg6lV012ezuTfdc0ouo6OEJ77autFJ56cghN9J8wZcRCoBOoCHfd3fLpNhBbDT2o7qxi8ZU7S65%2BPH%2FWRjeVvSZSeijxbML8GuI5HPPRCTBs6m2fGCF3wk%2BjZpaUXo9TJpf8tkA3q5%2BHvRDsVhAnXhK3DVUjtzNrZJPIv9xZ0%2BgIu6D81yoFilf%2FoMa5%2Bh8%3D%3C%2Fdiagram%3E%3C%2Fmxfile%3E)

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