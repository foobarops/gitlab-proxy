package com.example.gitlabproxy.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabClientClient.Group;

@SpringBootTest
@ActiveProfiles("client")
public class GitlabClientGroupsTest extends AbstractTest {
    
    @Autowired
    private GitlabClientClient gitlabGroupsClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testDeserialization() throws IOException {
        // Arrange
        String mockResponse = "[{\"id\": 1, \"name\": \"Test Group\", \"path\": \"test-group\", \"full_path\": \"path/test-group\"}]";
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&pagination=keyset&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        List<Group> groups = gitlabGroupsClient.getGroups();

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

    @Test
    void testDecodeURI() throws IOException {
        // Arrange
        String mockResponse = "[]";
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&pagination=keyset&order_by=name"))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON).headers(
                      new org.springframework.http.HttpHeaders() {{
                          add("link", "<https://gitlab.com/api/v4/groups?per_page=100&pagination=keyset&order_by=name&cursor=2%3D>; rel=\"next\"");
                      }}
                  ));
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&pagination=keyset&order_by=name&cursor=2%3D"))
                    .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        gitlabGroupsClient.getGroups();

        // Assert
        mockServer.verify();
    }
}
