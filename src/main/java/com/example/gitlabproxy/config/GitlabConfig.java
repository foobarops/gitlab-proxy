package com.example.gitlabproxy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.gitlabproxy.api.GitLabGroupsApi;

@Configuration
@EnableCaching
public class GitlabConfig {

    @Bean
    GitLabGroupsApi gitLabApi(@Value("${gitlab.api.url}") String apiUrl) {
        return new GitLabGroupsApi(apiUrl, null);
    }

}
