package com.example.gitlabproxy.client;

import lombok.RequiredArgsConstructor;
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
    public List<Group> getGroups(boolean refresh) {
        return gitlabGroupsClient.getGroups(refresh);
    }
}
