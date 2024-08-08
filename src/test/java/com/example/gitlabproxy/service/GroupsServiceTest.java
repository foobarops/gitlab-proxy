package com.example.gitlabproxy.service;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabClientClient.Group;
import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.client.GitlabClient;
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
    private GitlabClient gitlabClient;

    @AfterEach
    private void afterEach() {
        verifyNoMoreInteractions(gitlabClient);
    }

    @Test
    void testGetGroups() {
        // Arrange
        List<Group> groups = List.of(
            Group.builder().id(1).name("Test Group").path("test-group").fullPath("path/test-group").build(),
            Group.builder().id(2).name("Another Group").path("another-group").fullPath("path/another-group").build()
        );
        when(gitlabClient.getGroups(false)).thenReturn(groups);

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

        verify(gitlabClient).getGroups(false);
    }

    @Test
    void testGetGroupsFiltered() {
        // Arrange
        List<Group> groups = List.of(
            Group.builder().id(1).name("Test Group").path("test-group").fullPath("path/test-group").build(),
            Group.builder().id(2).name("Another Group").path("another-group").fullPath("path/another-group").build()
        );
        when(gitlabClient.getGroups(false)).thenReturn(groups);

        // Act
        GroupsWrapper groupsWrapper = groupsService.getGroups("test", false);

        // Assert
        softly.assertThat(groupsWrapper).isNotNull();
        softly.assertThat(groupsWrapper.getGroups()).isNotNull();
        softly.assertThat(groupsWrapper.getGroups()).hasSize(1);
        GroupsWrapper.Group group = groupsWrapper.getGroups().get(0);
        softly.assertThat(group).isNotNull();
        softly.assertThat(group.getId()).isEqualTo(1);
        softly.assertThat(group.getName()).isEqualTo("Test Group");
        softly.assertThat(group.getPath()).isEqualTo("test-group");
        softly.assertThat(group.getFullPath()).isEqualTo("path/test-group");

        verify(gitlabClient).getGroups(false);
    }
}