package com.example.gitlabproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.client.GitlabClientGroups;

@Configuration
@EnableCaching
public class GitlabConfig {
    
    @Profile("client")
    @EnableConfigurationProperties(GitlabClientGroups.Config.class)
    public static class ClientConfig {}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
