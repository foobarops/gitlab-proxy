package com.example.gitlabproxy.client;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.gitlabproxy.AbstractTest;
import com.example.gitlabproxy.client.GitlabGroupsClient.Group;

@SpringBootTest
@EnableCaching
@ActiveProfiles("client")
public class GitlabRetryableClientTest extends AbstractTest {

    @Autowired
    private GitlabRetryableClient gitlabRetryableClient;
 
    @MockBean
    private GitlabGroupsCachingClient gitlabGroupsCachingClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private static final String mockResponse = "[{\"id\": 1, \"name\": \"Test Group\", \"path\": \"test-group\", \"full_path\": \"path/test-group\"}, " +
                  "{\"id\": 2, \"name\": \"Second Group\", \"path\": \"second-group\", \"full_path\": \"path/second-group\"}]";

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    void verifyMocks() {
        mockServer.verify();
    }

    @Test
    void testGetGroups() throws URISyntaxException {
        // Arrange
        mockServer.expect(requestTo("https://gitlab.com/api/v4/groups?per_page=100&owned=false&page=0&sort=asc&statistics=false&with_custom_attributes=false&order_by=name&filter"))
                  .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        // Act
        gitlabRetryableClient.getGroups(null, 100, 0);
        // Assert
    }

    @Test
    void testDeserialization() throws IOException {
        // Arrange
        final String GROUPS_API_URL = "/abc";
        mockServer.expect(requestTo(GROUPS_API_URL))
                  .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // Act
        String url = gitlabRetryableClient.getNextPage(GROUPS_API_URL);

        // Assert
        verify(gitlabGroupsCachingClient).putGroup(new Group(1, "Test Group", "test-group", "path/test-group"));
        verify(gitlabGroupsCachingClient).putGroup(new Group(2, "Second Group", "second-group", "path/second-group"));
        softly.assertThat(url).isNull();

    }
}
