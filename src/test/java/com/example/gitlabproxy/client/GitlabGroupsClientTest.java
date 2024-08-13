package com.example.gitlabproxy.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabGroupsClient.Group;

@SpringBootTest
@ActiveProfiles("client")
public class GitlabGroupsClientTest extends AbstractTest {
    
    @Autowired
    private GitlabGroupsClient gitlabGroupsClient;

    @Autowired
    private RestTemplate restTemplate;

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

    @Test
    void testDeserialization() throws IOException {
        // Arrange
        String mockResponse = "[{\"id\": 1, \"name\": \"Test Group\", \"path\": \"test-group\", \"full_path\": \"path/test-group\"}, " +
                      "{\"id\": 2, \"name\": \"Second Group\", \"path\": \"second-group\", \"full_path\": \"path/second-group\"}]";
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&cursor=&owned=false&page=1&pagination=keyset&sort=asc&statistics=false&with_custom_attributes=false&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        List<Group> groups = gitlabGroupsClient.getGroups(false);

        // Assert
        softly.assertThat(groups).isNotNull();
        softly.assertThat(groups).hasSize(2);
        Group group = groups.get(0);
        softly.assertThat(group).isNotNull();
        softly.assertThat(group.getId()).isEqualTo(1);
        softly.assertThat(group.getName()).isEqualTo("Test Group");
        softly.assertThat(group.getPath()).isEqualTo("test-group");
        softly.assertThat(group.getFullPath()).isEqualTo("path/test-group");
        group = groups.get(1);
        softly.assertThat(group).isNotNull();
        softly.assertThat(group.getId()).isEqualTo(2);
        softly.assertThat(group.getName()).isEqualTo("Second Group");
        softly.assertThat(group.getPath()).isEqualTo("second-group");
        softly.assertThat(group.getFullPath()).isEqualTo("path/second-group");
    }

    @Test
    void testDecodeURI() throws IOException {
        // Arrange
        String mockResponse = "[]";
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&cursor=&owned=false&page=1&pagination=keyset&sort=asc&statistics=false&with_custom_attributes=false&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON).headers(
                      new org.springframework.http.HttpHeaders() {{
                          add("link", "<https://gitlab.com/api/v4/groups?per_page=100&pagination=keyset&order_by=name&cursor=2%3D>; rel=\"next\"");
                      }}
                  ));
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&pagination=keyset&order_by=name&cursor=2%3D"))
                    .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        gitlabGroupsClient.getGroups(false);

        // Assert
        mockServer.verify();
    }


    /**
     * Test case to verify that the cache works correctly.
     * 
     * This test method performs the following steps:
     * 1. Mock the GitLab API to return a list of groups.
     * 2. Call the getGroups method on the GitlabClient twice.
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
        gitlabGroupsClient.getGroups(false);
        gitlabGroupsClient.getGroups(false);

        // Assert
        mockServer.verify();
    }

    /*
     * Test case to verify that the cache is refreshed when requested.
     * 
     * This test method performs the following steps:
     * 1. Mock the GitLab API to return a list of groups.
     * 2. Call the getGroups method on the GitlabClient twice, once with refresh set to false and once with refresh set to true.
     * 3. Verify that the GitLab API is called twice.
     */
    @Test
    @DisplayName("Cache refresh")
    void cacheRefresh() throws IOException {
        // Arrange
        String mockResponse = "[{\"id\": 1, \"name\": \"Test Group\", \"path\": \"test-group\", \"full_path\": \"path/test-group\"}]";
        mockServer.expect(ExpectedCount.times(2), requestTo("https://gitlab.com/api/v4/groups?per_page=100&cursor=&owned=false&page=1&pagination=keyset&sort=asc&statistics=false&with_custom_attributes=false&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        gitlabGroupsClient.getGroups(false);
        gitlabGroupsClient.getGroups(true);

        // Assert
        mockServer.verify();
    }
}
