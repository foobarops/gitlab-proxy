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
     * This method is used to get the groups from the cache.
     * @return List<Group>
     */
    @Cacheable(value = "groupsCache", key = "'getGroups'")
    public List<Group> getGroups() {
        return fetchGroups();
    }

    /**
     * This method is used to refresh the cache along with returning the groups.
     * @return List<Group>
     */
    @CachePut(value = "groupsCache", key = "'getGroups'")
    public List<Group> getGroupsRefreshed() {
        return fetchGroups();
    }

    /**
     * This method fetches the groups from the GitLab API.
     * @return List<Group>
     */
    private List<Group> fetchGroups() {
        try {
            return gitLabApi.getGroupApi().getGroups(100).page(0);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }
}
