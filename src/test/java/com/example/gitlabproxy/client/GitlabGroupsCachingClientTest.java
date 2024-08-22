package com.example.gitlabproxy.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.gitlabproxy.AbstractTest;

@SpringBootTest
@ActiveProfiles("client")
public class GitlabGroupsCachingClientTest extends AbstractTest {
    
    @Autowired
    private GitlabGroupsClient gitlabGroupsClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GitlabScheduledClient gitlabScheduledClient;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Autowired
    private CacheManager cacheManager;

    @SuppressWarnings("null")
    @BeforeEach
    void evictCache() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }



    /**
     * Test case to verify that the cache works correctly.
     * 
     * This test method performs the following steps:
     * 1. Mock the GitLab API to return a list of groups.
     * 2. Call refreshGroups and then getGroups on the GitlabClient twice.
     * 3. Verify that the GitLab API is called only once.
     * 
     * @see com.example.gitlabproxy.client.GitlabGroupsClient#getGroups()
     */
    @Test
    @DisplayName("Cache works")
    void cacheWorks() throws IOException {
        // Arrange
        String mockResponse = "[{\"id\": 1, \"name\": \"Test Group\", \"path\": \"test-group\", \"full_path\": \"path/test-group\"}]";
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&cursor=&owned=false&page=1&pagination=keyset&sort=asc&statistics=false&with_custom_attributes=false&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        gitlabScheduledClient.refreshGroups();
        gitlabGroupsClient.getGroups(null, false);
        gitlabGroupsClient.getGroups(null, false);

        // Assert
        mockServer.verify();
    }

    /*
     * Test case to verify that the cache is refreshed when requested.
     * 
     * This test method performs the following steps:
     * 1. Mock the GitLab API to return a list of groups.
     * 2. Call the getGroups method on the GitlabClient twice with refresh set to true.
     * 3. Verify that the GitLab API is called twice.
     */
    @Test
    @DisplayName("Cache refresh")
    void cacheRefresh() throws IOException {
        // Arrange
        String mockResponse = "[]";
        mockServer.expect(ExpectedCount.times(2), anything())
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        gitlabGroupsClient.getGroups(null, true);
        gitlabGroupsClient.getGroups(null, true);

        // Assert
        mockServer.verify();
    }
}
