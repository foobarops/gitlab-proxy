package com.example.gitlabproxy.api.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class GroupsWrapper {

    List<Group> groups;

    @Value
    @Builder
    public static class Group {
        int id;
        String name;
        String path;
        String fullPath;
    }

}
