package com.example.gitlabproxy.client;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
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
    private final GitlabRetryableClient gitlabGroupsRetryableClient;
    final Gson gson;
    final Client.Config config;
	private final Client.Config.Groups groupsConfig;
    int fullRefreshCount = 0;

    @Scheduled(timeUnit = TimeUnit.HOURS, fixedRate = 6)
    public void refreshGroups() {
        log.info(fullRefreshCount++ + " - Refreshing groups");
		String url = config.getUrl() + groupsConfig.getStartKeysetUrl();
		int cycle = 0;
		while (url != null && config.shouldContinue(cycle++)) {
            log.debug(String.format("Cycle: %d. Getting groups from %s", cycle, url));
			url = gitlabGroupsRetryableClient.getNextPage(url);
            if (config.shouldLog(cycle)) {
        		log.info(String.format("Cycles done: %04d. Next URL: %s", cycle, url));
        	}
		}
		gitlabGroupsClient.setStateReady();;
	}

}
