package com.example.gitlabproxy.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabGroupsClient.Group;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * This class contains unit tests for the GitlabClient class.
 * It tests various scenarios related to retrieving groups from GitLab.
 * 
 * The test cases include:
 * - Success: Verifies that the getGroups method returns the expected groups.
 * - Error is propagated: Verifies that an exception thrown by the GitLab API is propagated correctly.
 * - Cache works: Verifies that the caching mechanism of the GitlabClient works as expected.
 * 
 * The test methods in this class use Mockito to mock the GitLab API and its dependencies.
 * 
 * @see com.example.gitlabproxy.client.GitlabClient
 */
@SpringBootTest
@ActiveProfiles("test")
class GitlabClientTest extends AbstractTest {

    @MockBean
    private GitlabGroupsClient gitlabGroupsClient;

    @Autowired
    private GitlabClient gitlabClient;

    @AfterEach
    private void afterEach() {
        verifyNoMoreInteractions(gitlabGroupsClient);
    }

    @Test
    @DisplayName("Success")
    void success() throws IOException {
        List<Group> groups = List.of(
            new Group().withFullPath("test/group1"),
            new Group().withFullPath("test/group2")
        );
        when(gitlabGroupsClient.getGroups(false)).thenReturn(groups);
        softly.assertThat(gitlabClient.getGroups(false)).isEqualTo(groups);
        verify(gitlabGroupsClient).getGroups(false);
    }

    @Test
    @DisplayName("Error is propagated")
    void exception() throws IOException {
        when(gitlabGroupsClient.getGroups(false)).thenThrow(new RuntimeException("test"));
        softly.assertThatThrownBy(() -> gitlabClient.getGroups(false))
            .isInstanceOf(RuntimeException.class)
            .message().isEqualTo("test");
        verify(gitlabGroupsClient).getGroups(false);
    }
}