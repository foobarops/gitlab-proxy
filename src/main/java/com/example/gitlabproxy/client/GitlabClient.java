package com.example.gitlabproxy.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitlabClient {
    
    private final GitlabGroupsClient gitlabGroupsClient;

    public GitlabGroupsClient getGitlabGroupsClient() {
        return gitlabGroupsClient;
    }
}
