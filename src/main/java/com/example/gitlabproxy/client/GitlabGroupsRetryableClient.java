package com.example.gitlabproxy.client;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.client.GitlabGroupsClient.Group;
import com.example.gitlabproxy.config.AppConfig.Client;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("client")
@Component
@Retryable(
    value = {
         ResourceAccessException.class,
         HttpServerErrorException.class,
         JsonParseException.class,
         JsonSyntaxException.class,
         },
    maxAttempts = 5,
    backoff = @Backoff(delay = 2000, multiplier = 2)
)
@RequiredArgsConstructor
public class GitlabGroupsRetryableClient {

    private final RestTemplate restTemplate;
    private final Gson gson;
    private final Client.Config config;

    String getGroups(String decodedUrl, HttpEntity<String> entity, List<Group> result, int cycle) {
    	// Make the request
    	ResponseEntity<String> response = restTemplate.exchange(decodedUrl, HttpMethod.GET, entity, String.class);
    
    	// Get the 'link' header from the response
    	String url = response.getHeaders().getFirst("link");
    
    	// Parse the response
    	Collection<? extends Group> nextPage = gson.fromJson(response.getBody(), new TypeToken<List<Group>>(){}.getType());
    	if (nextPage != null && !nextPage.isEmpty()) {
    		result.addAll(nextPage);
    		if (config.shouldLog(cycle)) {
    			log.info(String.format("Cycles done: %04d. Path of last item: %s. Next URL: %s", cycle, result.get(result.size() - 1).getFullPath(), url));
    		}
    	}
    	return url;
    }

}