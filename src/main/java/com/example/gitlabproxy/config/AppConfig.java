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
@EnableCaching
public class AppConfig {
    
    @Profile("client")
    @EnableConfigurationProperties(Client.Config.class)
    public static class Client {

        @ConstructorBinding
        @ConfigurationProperties(prefix = "gitlab.api")
        @RequiredArgsConstructor
        @Getter
        public static class Config {
        	private final String url;
        	private final String privateToken;
        }}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
