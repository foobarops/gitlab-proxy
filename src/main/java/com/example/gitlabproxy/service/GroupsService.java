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
     * Returns info about publicly available GitLab groups
     *
     * @return wrapped list of group info
     */
    public GroupsWrapper getGroups(boolean refresh) {
        return GroupsWrapper.builder()
            .groups((
                refresh
                    ? gitlabClient.getGroups(true)
                    : gitlabClient.getGroups()
                ).stream()
                .map(group -> GroupsWrapper.Group.builder()
                    .fullPath(group.getFullPath()).build())
                .collect(Collectors.toList()))
            .build();
    }
    /**
     * Returns info about publicly available GitLab groups without forcing a refresh
     *
     * @return wrapped list of group info
     */
    public GroupsWrapper getGroups() {
        return getGroups(false);
    }
}
