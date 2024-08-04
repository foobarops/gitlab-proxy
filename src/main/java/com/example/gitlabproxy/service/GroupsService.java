package com.example.gitlabproxy.service;

import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.client.GitlabClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupsService {

    private final GitlabClient gitlabClient;

    /**
     * Get groups from Gitlab optionally filtered by a string and optionally refreshed
     * 
     * @param filter Filter to apply
     * @param refresh Whether to force a cache refresh
     * @return Wrapped list of group infos
     */
    public GroupsWrapper getGroups(String filter, boolean refresh) {
        return GroupsWrapper.builder()
            .groups(gitlabClient.getGroups(refresh).stream()
            .filter(group -> filter == null || group.getFullPath().contains(filter))
            .map(group -> GroupsWrapper.Group.builder()
                .fullPath(group.getFullPath()).build())
            .collect(Collectors.toList()))
            .build();
    }
}
