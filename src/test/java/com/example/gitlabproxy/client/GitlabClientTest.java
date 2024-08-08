package com.example.gitlabproxy.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.example.gitlabproxy.AbstractTest;

import static org.mockito.Mockito.*;

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

    /*
     * Test for GitlabClient.getGroups
     * 
     * The test checks that the method returns the instance of GitlabGroupsClient
     */
    @Test
    @DisplayName("Success")
    void getGitlabGroupsClient() {
        GitlabGroupsClient gitlabGroupsClient = gitlabClient.getGitlabGroupsClient();
        softly.assertThat(gitlabGroupsClient).isNotNull();
    }
}