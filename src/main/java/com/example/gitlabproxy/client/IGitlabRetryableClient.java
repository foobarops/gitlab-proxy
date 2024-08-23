package com.example.gitlabproxy.client;

import java.util.List;

import com.example.gitlabproxy.client.GitlabGroupsClient.Group;

public interface IGitlabRetryableClient {

    String getNextPage(String url);

    List<Group> getGroups(String filter, int size, int page);

}