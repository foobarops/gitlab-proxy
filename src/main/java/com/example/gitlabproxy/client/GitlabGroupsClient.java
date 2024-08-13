package com.example.gitlabproxy.client;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.example.gitlabproxy.config.AppConfig.Client;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("client")
@Component
@RequiredArgsConstructor
public class GitlabGroupsClient {
	
	private final Client.Config config;
	private final Client.Config.Groups groupsConfig;

	private final GitlabGroupsRetryableClient retryableClient;

	@Cacheable(value = "groupsCache", key = "#root.methodName")
	@CachePut(value = "groupsCache", key = "#root.methodName", condition = "#refresh")
	public List<Group> getGroups(boolean refresh) {
		String url = config.getUrl() + groupsConfig.getUrl();
		List<Group> result = new ArrayList<>();
		int cycle = 0;
		while (url != null && config.shouldContinue(cycle++)) {
			// Create headers
			HttpHeaders headers = new HttpHeaders();
			headers.set("Private-Token", config.getPrivateToken());

			// Create entity with headers
			HttpEntity<String> entity = new HttpEntity<>(headers);

			// Decode the URI
			String decodedUrl = decodeUri(url);

			url = retryableClient.getGroups(decodedUrl, entity, result, cycle);
		}
		return result;
	}

	private String decodeUri(String uri) {
		try {
			return URLDecoder.decode(uri.replaceFirst("^<", "").replaceFirst(">.*", ""), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to decode URL", e);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@Builder
	public static class Group implements Serializable {
		@With private int id;
		@With private String name;
		@With private String path;
		@With @SerializedName("full_path") private String fullPath;
	}
}
