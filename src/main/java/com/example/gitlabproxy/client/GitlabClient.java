package com.example.gitlabproxy.client;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import com.example.gitlabproxy.client.GitlabClientClient.Group;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GitlabClient {
    
    private final GitlabClientClient gitlabGroupsClient;

    /**
     * This method is used to get the groups from the cache. Optionally, it can refresh the cache.
     * @param refresh boolean whether to refresh the cache
     * @return List<Group>
     */
    @Cacheable(value = "groupsCache", key = "#root.methodName")
    @CachePut(value = "groupsCache", key = "#root.methodName", condition = "#refresh")
    public List<Group> getGroups(boolean refresh) {
        return gitlabGroupsClient.getGroups();
    }
}
