package com.example.gitlabproxy.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.config.AppConfig.Client;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("client")
@Component
@RequiredArgsConstructor
public class GitlabScheduledClient {

    final RestTemplate restTemplate;
    final GitlabGroupsClient gitlabGroupsClient;
    private final GitlabGroupsRetryableClient gitlabGroupsRetryableClient;
    final Gson gson;
    final Client.Config config;
	private final Client.Config.Groups groupsConfig;
    int fullRefreshCount = 0;

	private String decodeUri(String uri) {
		try {
			return URLDecoder.decode(uri.replaceFirst("^<", "").replaceFirst(">.*", ""), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to decode URL", e);
		}
	}

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 6)
    public void refreshGroups() {
        log.info(fullRefreshCount++ + " - Refreshing groups");
		String url = config.getUrl() + groupsConfig.getUrl();
		int cycle = 0;
		while (url != null && config.shouldContinue(cycle++)) {
			// Create headers
			HttpHeaders headers = new HttpHeaders();
			headers.set("Private-Token", config.getPrivateToken());

			// Create entity with headers
			HttpEntity<String> entity = new HttpEntity<>(headers);

			// Decode the URI
			String decodedUrl = decodeUri(url);
            url = gitlabGroupsRetryableClient.getNextPage(cycle, entity, decodedUrl);
		}
		gitlabGroupsClient.setStateReady();;
	}

}
