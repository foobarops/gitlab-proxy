package com.example.gitlabproxy.service;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.api.GitLabGroupsApi.Group;
import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.client.GitlabClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class GroupsServiceTest extends AbstractTest {

    @Autowired
    private GroupsService groupsService;

    @MockBean
    private GitlabClient gitlabClient;

    @AfterEach
    private void afterEach() {
        verifyNoMoreInteractions(gitlabClient);
    }

    @Test
    void getGroupNames() {
        when(gitlabClient.getGroups(false)).thenReturn(
                List.of(
                        new Group().withFullPath("test/group1"),
                        new Group().withFullPath("test/group2")
                )
        );
        softly.assertThat(
            groupsService.getGroups(null, false).getGroups().stream()
                .map(GroupsWrapper.Group::getFullPath)
                .collect(Collectors.toList())
            ).isEqualTo(List.of("test/group1", "test/group2"));
        verify(gitlabClient).getGroups(false);
    }

    @Test
    void getGroupNamesRefreshed() {
        when(gitlabClient.getGroups(true)).thenReturn(
                List.of(
                        new Group().withFullPath("test/group1"),
                        new Group().withFullPath("test/group2")
                )
        );
        softly.assertThat(
            groupsService.getGroups(null, true).getGroups().stream()
                .map(GroupsWrapper.Group::getFullPath)
                .collect(Collectors.toList())
            ).isEqualTo(List.of("test/group1", "test/group2"));
        verify(gitlabClient).getGroups(true);
    }

    @Test
    void getGroupNamesFiltered() {
        when(gitlabClient.getGroups(false)).thenReturn(
                List.of(
                        new Group().withFullPath("test/group1"),
                        new Group().withFullPath("test/group2"),
                        new Group().withFullPath("test/group3")
                )
        );
        softly.assertThat(
            groupsService.getGroups("group2", false).getGroups().stream()
                .map(GroupsWrapper.Group::getFullPath)
                .collect(Collectors.toList())
            ).isEqualTo(List.of("test/group2"));
        verify(gitlabClient).getGroups(false);
    }

    @Test
    void getGroupNamesFilteredRefreshed() {
        when(gitlabClient.getGroups(true)).thenReturn(
                List.of(
                        new Group().withFullPath("test/group1"),
                        new Group().withFullPath("test/group2"),
                        new Group().withFullPath("test/group3")
                )
        );
        softly.assertThat(
            groupsService.getGroups("group2", true).getGroups().stream()
                .map(GroupsWrapper.Group::getFullPath)
                .collect(Collectors.toList())
            ).isEqualTo(List.of("test/group2"));
        verify(gitlabClient).getGroups(true);
    }
}