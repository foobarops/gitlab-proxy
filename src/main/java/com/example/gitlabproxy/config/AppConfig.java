package com.example.gitlabproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Configuration
public class AppConfig {
    
    @Profile("client")
    @EnableCaching
    @EnableConfigurationProperties(Client.Config.class)
    public static class Client {

        @ConstructorBinding
        @ConfigurationProperties(prefix = "gitlab.api")
        @RequiredArgsConstructor
        @Getter
        public static class Config {
        	private final String url;
            private final int maxCycles;
        	private final String privateToken;
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
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
