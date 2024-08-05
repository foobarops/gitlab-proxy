package com.example.gitlabproxy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.api.GitLabGroupsApi;

@Configuration
@EnableCaching
public class GitlabConfig {

    @Profile("default")
    @Bean
    GitLabGroupsApi gitLabGroupsApi(
        @Value("${gitlab.api.url}") String apiUrl,
        RestTemplate restTemplate
    ) {
        return new GitLabGroupsApi(apiUrl, null, restTemplate);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
