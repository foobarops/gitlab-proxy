package com.example.gitlabproxy.client;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.gitlabproxy.config.AppConfig.Client;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

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

	private final RestTemplate restTemplate;
	private final Gson gson = new Gson();

	@Cacheable(value = "groupsCache", key = "#root.methodName")
	@CachePut(value = "groupsCache", key = "#root.methodName", condition = "#refresh")
	public List<Group> getGroups(boolean refresh) {
		String url = config.getUrl() + "/groups?per_page=100&pagination=keyset&order_by=name";
		List<Group> result = new ArrayList<>();
		int cycles = 0;
		while (url != null && config.shouldContinue(cycles++)) {
			// Create headers
			HttpHeaders headers = new HttpHeaders();
			headers.set("Private-Token", config.getPrivateToken());

			// Create entity with headers
			HttpEntity<String> entity = new HttpEntity<>(headers);

			// Decode the URI
			String decodedUrl = decodeUri(url);

			// Make the request
			ResponseEntity<String> response = restTemplate.exchange(decodedUrl, HttpMethod.GET, entity, String.class);
			
			// Parse the response
			Collection<? extends Group> nextPage = gson.fromJson(response.getBody(), new TypeToken<List<Group>>(){}.getType());
			if (nextPage != null && !nextPage.isEmpty()) {
				result.addAll(nextPage);
				if (config.shouldLog(cycles)) {
					log.debug(String.format("Cycles done: %04d. Path of last item: %s. Next URL: %s", cycles, result.get(result.size() - 1).getFullPath(), url));
				}
			}

			// Get the 'link' header from the response
			url = response.getHeaders().getFirst("link");
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
