package com.example.gitlabproxy.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabGroupsClient.Group;

@SpringBootTest
@ActiveProfiles("client")
public class GitlabGroupsClientTest extends AbstractTest {
    
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

    @AfterEach
    void verifyMocks() {
        mockServer.verify();
    }

    @Test
    void testGetGroupsFiltered() {
        // Arrange
        String mockResponse = "[{\"id\": 1, \"name\": \"Test Group\", \"path\": \"test-group\", \"full_path\": \"path/test-group\"}, " +
        "{\"id\": 2, \"name\": \"Second Group\", \"path\": \"second-group\", \"full_path\": \"path/second-group\"}]";
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&cursor=&owned=false&page=1&pagination=keyset&sort=asc&statistics=false&with_custom_attributes=false&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));
        // Act
        gitlabScheduledClient.refreshGroups();
        List<Group> groups = gitlabGroupsClient.getGroups("test", false);

        // Assert
        softly.assertThat(groups).isNotNull();
        softly.assertThat(groups).hasSize(1);
        Group group = groups.get(0);
        softly.assertThat(group).isNotNull();
        softly.assertThat(group.getId()).isEqualTo(1);
        softly.assertThat(group.getName()).isEqualTo("Test Group");
        softly.assertThat(group.getPath()).isEqualTo("test-group");
        softly.assertThat(group.getFullPath()).isEqualTo("path/test-group");

    }

    /*
     * Test case to verify that the bulkhead mode works correctly.
     * 
     * This test method performs the following steps:
     * 1. Mock the GitLab API to return an empty list of groups.
     * 2. Call getGroups on the GitlabClient in bulkhead mode.
     * 3. Verify that the GitLab API is called only once.
     */
    @Test
    void testGetGroupsInBulkheadMode() {
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&owned=false&page=0&sort=asc&statistics=false&with_custom_attributes=false&order_by=name&filter"))
                  .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        gitlabGroupsClient.getGroups(null, true);
    }
}
