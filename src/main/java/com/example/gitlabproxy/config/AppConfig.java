package com.example.gitlabproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Configuration
public class AppConfig {
    
    @Profile("client")
    @EnableCaching
    @EnableRetry
    @EnableConfigurationProperties(Client.Config.class)
    public static class Client {

        @ConstructorBinding
        @ConfigurationProperties(prefix = "gitlab.api")
        @RequiredArgsConstructor
        @Getter
        public static class Config {
        	private final String url;
            private final int maxCycles;
            private final int logCycles;
        	private final String privateToken;
            private final String cursor;
            /*
             * Check if the client should continue making requests
             * 
             * Intended for integration tests
             * Used to limit the number of requests made with keyset pagination
             * @param cycle Current cycle starting at 0
             * @return Whether to continue
             */
            public boolean shouldContinue(int cycle) {
                return maxCycles == 0 || cycle < maxCycles;
            }
            /*
             * Check if the client should log the current cycle
             * 
             * Intended for integration tests
             * Used to log the current cycle
             * @param cycle Current cycle starting at 1
             * @return Whether to log
             */
            public boolean shouldLog(int cycle) {
                return logCycles > 0 && cycle % logCycles == 0;
            }
            /*
             * Get the starting URL for the groups
             * 
             * If a cursor is provided, it will be appended to the URL. This is for testing purposes only
             * @return The URL
             */
            public String getGroupUrl() {
                return String.format("%s/groups?per_page=100&cursor=%s&owned=false&page=1&pagination=keyset&sort=asc&statistics=false&with_custom_attributes=false&order_by=name", url, cursor == null ? "" : cursor);
            }
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
