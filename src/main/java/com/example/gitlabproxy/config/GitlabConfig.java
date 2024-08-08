package com.example.gitlabproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    @EnableConfigurationProperties(GitLabGroupsApi.Config.class)
    public static class DefaultConfig {}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
