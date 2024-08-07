package com.example.gitlabproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.api.GitLabGroupsApi;

@Configuration
@EnableCaching
@EnableConfigurationProperties(GitLabGroupsApi.Config.class)
public class GitlabConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
