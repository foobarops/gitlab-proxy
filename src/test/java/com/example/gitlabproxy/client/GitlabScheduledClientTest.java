package com.example.gitlabproxy.client;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("client")
public class GitlabScheduledClientTest {

    @Autowired
    private GitlabScheduledClient gitlabScheduledClient;
 
    @Autowired
    private RestTemplate restTemplate;

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
        gitlabScheduledClient.refreshGroups();

        // Assert
        mockServer.verify();
    }

}
