package com.example.gitlabproxy.api;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

@Profile("default")
@Component
@RequiredArgsConstructor
public class GitLabGroupsApi {
	
	@ConstructorBinding
	@ConfigurationProperties(prefix = "gitlab.api")
	@RequiredArgsConstructor
	@Getter
	public static class Config {
		private final String url;
		private final String privateToken;
	}

	private final Config config;

	private final RestTemplate restTemplate;
	private final Gson gson = new Gson();

	public List<Group> getGroups() {
		String url = config.getUrl() + "/groups?per_page=100&pagination=keyset&order_by=name";
		List<Group> result = new ArrayList<>();
		int cycles = 0;
		while (url != null && cycles++ < 2) {
			// Create headers
			HttpHeaders headers = new HttpHeaders();
			headers.set("Private-Token", config.getPrivateToken());

			// Create entity with headers
			HttpEntity<String> entity = new HttpEntity<>(headers);

			// Decode the URI
			String decodedUrl = decodeUri(url);

			// Make the request
			ResponseEntity<String> response = restTemplate.exchange(decodedUrl, HttpMethod.GET, entity, String.class);

			// Get the 'link' header from the response
			url = response.getHeaders().getFirst("link");

			// Parse the response
			result.addAll(gson.fromJson(response.getBody(), new TypeToken<List<Group>>(){}.getType()));
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
