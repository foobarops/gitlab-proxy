package com.example.gitlabproxy.client;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import com.example.gitlabproxy.AbstractTest;

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
    private GitLabApi gitLabApi;

    @MockBean
    private GroupApi mockGroupApi;

    @Autowired
    private GitlabClient gitlabClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        when(gitLabApi.getGroupApi()).thenReturn(mockGroupApi);
    }

    @SuppressWarnings("null")
    @BeforeEach
    void evictCache() {
        cacheManager.getCache("groupsCache").clear();
    }

    @AfterEach
    private void afterEach() {
        verifyNoMoreInteractions(gitLabApi, mockGroupApi);
    }

    @Test
    @DisplayName("Success")
    void success() throws GitLabApiException {
        List<Group> groups = List.of(
            new Group().withFullName("test/group1"),
            new Group().withFullName("test/group2")
        );
        @SuppressWarnings("unchecked")
        Pager<Group> mockPager = mock(Pager.class);
        when(mockGroupApi.getGroups(100)).thenReturn(mockPager);
        when(mockPager.page(0)).thenReturn(groups);
        softly.assertThat(gitlabClient.getGroups()).isEqualTo(groups);
        verify(gitLabApi).getGroupApi();
        verify(mockGroupApi).getGroups(100);
    }

    @Test
    @DisplayName("Error is propagated")
    void exception() throws GitLabApiException {
        when(mockGroupApi.getGroups(100)).thenThrow(new GitLabApiException("test"));
        softly.assertThatThrownBy(() -> gitlabClient.getGroups())
            .isInstanceOf(RuntimeException.class)
            .getCause()
            .isInstanceOf(GitLabApiException.class)
            .message().isEqualTo("test");
        verify(gitLabApi).getGroupApi();
        verify(mockGroupApi).getGroups(100);
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
    void cacheWorks() throws GitLabApiException {
        List<Group> groups = List.of(
            new Group().withFullName("test/group1"),
            new Group().withFullName("test/group2")
        );

        @SuppressWarnings("unchecked")
        Pager<Group> mockPager = mock(Pager.class);
        when(mockGroupApi.getGroups(100)).thenReturn(mockPager);
        when(mockPager.page(0)).thenReturn(groups);

        // First call
        List<Group> firstCallResult = gitlabClient.getGroups();
        softly.assertThat(firstCallResult).isEqualTo(groups);

        // Second call
        List<Group> secondCallResult = gitlabClient.getGroups();
        softly.assertThat(secondCallResult.toString()).isEqualTo(groups.toString());

        verify(gitLabApi, times(1)).getGroupApi();
        verify(mockGroupApi, times(1)).getGroups(100);
    }
}