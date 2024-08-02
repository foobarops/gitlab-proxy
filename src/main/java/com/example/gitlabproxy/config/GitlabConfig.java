package com.example.gitlabproxy.config;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class GitlabConfig {

    @Bean
    GitLabApi gitLabApi(@Value("${gitlab.api.host.url}") String hostUrl) {
        return new GitLabApi(hostUrl, null);
    }

}
