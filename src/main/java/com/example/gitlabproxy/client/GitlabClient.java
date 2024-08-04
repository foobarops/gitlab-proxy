package com.example.gitlabproxy.client;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GitlabClient {
    
    private final GitLabApi gitLabApi;

    /**
     * This method is used to get the groups from the cache. Optionally, it can refresh the cache.
     * @param refresh boolean whether to refresh the cache
     * @return List<Group>
     */
    @Cacheable(value = "groupsCache", key = "#root.methodName")
    @CachePut(value = "groupsCache", key = "#root.methodName", condition = "#refresh")
    public List<Group> getGroups(boolean refresh) {
        try {
            return gitLabApi.getGroupApi().getGroups(100).page(0);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }
}
