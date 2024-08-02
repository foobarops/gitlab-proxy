package com.example.gitlabproxy.service;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.client.GitlabClient;
import org.gitlab4j.api.models.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

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
        when(gitlabClient.getGroups()).thenReturn(
                List.of(
                        new Group().withFullPath("test/group1"),
                        new Group().withFullPath("test/group2")
                )
        );
        softly.assertThat(
            groupsService.getGroups().getGroups().stream()
                .map(GroupsWrapper.Group::getFullPath)
                .collect(Collectors.toList())
            ).isEqualTo(List.of("test/group1", "test/group2"));
        verify(gitlabClient).getGroups();
    }
}