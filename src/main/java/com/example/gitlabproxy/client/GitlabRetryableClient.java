package com.example.gitlabproxy.client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
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
    backoff = @Backoff(delay = 2000, multiplier = 2),
    listeners = "loggingRetryListener"
)
@RequiredArgsConstructor
public class GitlabRetryableClient {

    private final RestTemplate restTemplate;
    private final GitlabGroupsCachingClient gitlabGroupsCachingClient;
    private final Gson gson;
    private final Client.Config config;

    public String getNextPage(String url) {
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", config.getPrivateToken());

        // Create entity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        
        // Parse the response
        List<Group> nextPage = gson.fromJson(response.getBody(), new TypeToken<List<Group>>(){}.getType());
        if (nextPage != null && !nextPage.isEmpty()) {
        	nextPage.forEach(gitlabGroupsCachingClient::putGroup);
        }
        return decodeLink(response.getHeaders().getFirst("link"));
    }

    public List<Group> getGroups(String filter, int size, int page) {
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", config.getPrivateToken());

        // Create entity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        URI uri;
        try{
            uri = new URIBuilder(config.getUrl() + "/groups")
            .addParameter("per_page", Integer.toString(size))
            .addParameter("owned", "false")
            .addParameter("page", Integer.toString(page))
            .addParameter("sort", "asc")
            .addParameter("statistics", "false")
            .addParameter("with_custom_attributes", "false")
            .addParameter("order_by", "name")
            .addParameter("filter", filter)
            .build();
        } catch (URISyntaxException e) {
            throw new RestClientException("Failed to build URI", e);
        }
        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        
        // Parse the response
        List<Group> groups = gson.fromJson(response.getBody(), new TypeToken<List<Group>>(){}.getType());
        return groups;
    }

	private String decodeLink(String link) {
        if (link == null) {
            return null;
        }
		try {
			return URLDecoder.decode(link.replaceFirst("^<", "").replaceFirst(">.*", ""), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to decode URL", e);
		}
	}

}
