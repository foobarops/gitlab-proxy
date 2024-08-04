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

    @Cacheable(value = "groupsCache", key = "#root.methodName")
    public List<Group> getGroups() {
        try {
            return gitLabApi.getGroupApi().getGroups(100).page(0);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to refresh the cache along with returning the groups.
     * @param forceRefresh
     * @return
     */
    @CachePut(value = "groupsCache", key = "#root.methodName")
    public List<Group> getGroups(boolean forceRefresh) {
        // Call the original method to get the result
        return getGroups();
    }
}
