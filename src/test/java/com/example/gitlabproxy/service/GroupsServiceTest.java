package com.example.gitlabproxy.service;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabGroupsClient.Group;
import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.client.GitlabGroupsClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class GroupsServiceTest extends AbstractTest {

    @Autowired
    private GroupsService groupsService;

    @MockBean
    private GitlabGroupsClient gitlabGroupsClient;

    @AfterEach
    private void afterEach() {
        verifyNoMoreInteractions(gitlabGroupsClient);
    }

    @Test
    void testGetGroups() {
        // Arrange
        List<Group> groups = List.of(
            Group.builder().id(1).name("Test Group").path("test-group").fullPath("path/test-group").build(),
            Group.builder().id(2).name("Another Group").path("another-group").fullPath("path/another-group").build()
        );
        when(gitlabGroupsClient.getGroups(null, false)).thenReturn(groups);

        // Act
        GroupsWrapper groupsWrapper = groupsService.getGroups(null, false);

        // Assert
        softly.assertThat(groupsWrapper).isNotNull();
        softly.assertThat(groupsWrapper.getGroups()).isNotNull();
        softly.assertThat(groupsWrapper.getGroups()).hasSize(2);
        GroupsWrapper.Group group1 = groupsWrapper.getGroups().get(0);
        softly.assertThat(group1).isNotNull();
        softly.assertThat(group1.getId()).isEqualTo(1);
        softly.assertThat(group1.getName()).isEqualTo("Test Group");
        softly.assertThat(group1.getPath()).isEqualTo("test-group");
        softly.assertThat(group1.getFullPath()).isEqualTo("path/test-group");
        GroupsWrapper.Group group2 = groupsWrapper.getGroups().get(1);
        softly.assertThat(group2).isNotNull();
        softly.assertThat(group2.getId()).isEqualTo(2);
        softly.assertThat(group2.getName()).isEqualTo("Another Group");
        softly.assertThat(group2.getPath()).isEqualTo("another-group");
        softly.assertThat(group2.getFullPath()).isEqualTo("path/another-group");

        verify(gitlabGroupsClient).getGroups(null, false);
    }
}