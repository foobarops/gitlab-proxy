package com.example.gitlabproxy.client;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.example.gitlabproxy.client.GitlabGroupsClient.Group;
import com.example.gitlabproxy.config.AppConfig.Client;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("client & stub")
@Component
@Retryable(
    value = {
         ResourceAccessException.class,
         HttpServerErrorException.class,
         JsonParseException.class,
         JsonSyntaxException.class,
         },
    maxAttempts = 5,
    backoff = @Backoff(delay = 2000, multiplier = 2),
    listeners = "loggingRetryListener"
)
@RequiredArgsConstructor
public class GitlabRetryableClientStub implements IGitlabRetryableClient {

    private final GitlabGroupsCachingClient gitlabGroupsCachingClient;
    private final Client.Config config;
    private final Client.Config.Groups groupConfig;

    @Override
    public String getNextPage(String url) {
        for (int i = 0; groupConfig.stubContinue(i); i++) {
            Group group = Group.builder()
            .id(i)
            .name("Group " + i)
            .path("group-" + i)
            .fullPath("path/group-" + i)
            .build(); // Create a new Group object
            // Add the group to the caching client
            gitlabGroupsCachingClient.putGroup(group);
            if (i%100 == 0 && config.shouldLog(i/100)) {
                log.info(String.format("Cycles done: %4d", i));
            }
        }
        return null;
    }

    @Override
    public List<Group> getGroups(String filter, int size, int page) {
        // Create headers
        throw new UnsupportedOperationException("Not implemented");
    }

}
