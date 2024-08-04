package com.example.gitlabproxy.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.api.GitLabGroupsApi;
import com.example.gitlabproxy.api.GitLabGroupsApi.Group;

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
class GitlabClientTest extends AbstractTest {

    @MockBean
    private GitLabGroupsApi gitLabGroupsApi;

    @Autowired
    private GitlabClient gitlabClient;

    @Autowired
    private CacheManager cacheManager;

    @SuppressWarnings("null")
    @BeforeEach
    void evictCache() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @AfterEach
    private void afterEach() {
        verifyNoMoreInteractions(gitLabGroupsApi);
    }

    @Test
    @DisplayName("Success")
    void success() throws IOException {
        List<Group> groups = List.of(
            new Group().withFullPath("test/group1"),
            new Group().withFullPath("test/group2")
        );
        when(gitLabGroupsApi.getGroups()).thenReturn(groups);
        softly.assertThat(gitlabClient.getGroups(false)).isEqualTo(groups);
        verify(gitLabGroupsApi).getGroups();
    }

    @Test
    @DisplayName("Error is propagated")
    void exception() throws IOException {
        when(gitLabGroupsApi.getGroups()).thenThrow(new IOException("test"));
        softly.assertThatThrownBy(() -> gitlabClient.getGroups(false))
            .isInstanceOf(RuntimeException.class)
            .getCause()
            .isInstanceOf(IOException.class)
            .message().isEqualTo("test");
        verify(gitLabGroupsApi).getGroups();
    }

    /**
     * Test case to verify that the cache works correctly.
     * 
     * This test method performs the following steps:
     * 1. Creates a list of groups.
     * 2. Sets up the necessary mock objects for the GitLab API.
     * 3. Calls the `getGroups` method of the `gitlabClient` object twice.
     * 4. Verifies that the results of both calls are equal to the initial list of groups.
     * 5. Verifies that the `getGroupApi` method of the `gitLabApi` object is called only once.
     * 6. Verifies that the `getGroups` method of the `mockGroupApi` object is called only once with the correct parameter.
     * 
     * @see com.example.gitlabproxy.client.GitlabClient#getGroups()
     */
    @Test
    @DisplayName("Cache works")
    void cacheWorks() throws IOException {
        List<Group> groups = List.of(
            new Group().withFullPath("test/group1"),
            new Group().withFullPath("test/group2")
        );

        when(gitLabGroupsApi.getGroups()).thenReturn(groups);

        // First call
        List<Group> firstCallResult = gitlabClient.getGroups(false);
        softly.assertThat(firstCallResult).isEqualTo(groups);

        // Second call
        List<Group> secondCallResult = gitlabClient.getGroups(false);
        softly.assertThat(secondCallResult.toString()).isEqualTo(groups.toString());

        verify(gitLabGroupsApi, times(1)).getGroups();
    }
    
    @Test
    @DisplayName("Cache refresh works")
    void cacheRefreshWorks() throws IOException {
        List<Group> groups = List.of(
            new Group().withFullPath("test/group1"),
            new Group().withFullPath("test/group2")
        );

        when(gitLabGroupsApi.getGroups()).thenReturn(groups);

        // First call
        List<Group> firstCallResult = gitlabClient.getGroups(false);
        softly.assertThat(firstCallResult).isEqualTo(groups);

        // Second call
        List<Group> secondCallResult = gitlabClient.getGroups(true);
        softly.assertThat(secondCallResult.toString()).isEqualTo(groups.toString());

        verify(gitLabGroupsApi, times(2)).getGroups();
    }
}