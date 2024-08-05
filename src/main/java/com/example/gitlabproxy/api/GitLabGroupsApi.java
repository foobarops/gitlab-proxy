package com.example.gitlabproxy.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

@RequiredArgsConstructor
public class GitLabGroupsApi {

	private final String apiUrl;
	private final String privateToken;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

	public List<Group> getGroups() {
		String url = apiUrl + "/groups?per_page=100&pagination=keyset&order_by=name";
		List<Group> result = new ArrayList<>();
        int cycles = 0;
		while (url != null && cycles++ < 2) {
			// Create headers
			HttpHeaders headers = new HttpHeaders();
			headers.set("Private-Token", privateToken);

			// Create entity with headers
			HttpEntity<String> entity = new HttpEntity<>(headers);

			// Make the request
			ResponseEntity<String> response = restTemplate.exchange(url.replaceFirst("^<", "").replaceFirst(">.*", ""), HttpMethod.GET, entity, String.class);
			// Get the 'link' header from the response
			url = response.getHeaders().getFirst("link");
			// Parse the response
			result.addAll(gson.fromJson(response.getBody(), new TypeToken<List<Group>>(){}.getType()));
		}
		return result;
	}

    @Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public static class Group implements Serializable {
		@With private int id;
		@With private String name;
		@With private String path;
		@With @SerializedName("full_path") private String fullPath;
	}
}
