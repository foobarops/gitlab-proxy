package com.example.gitlabproxy.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private GitlabScheduledClient gitlabScheduledClient;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
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
}
