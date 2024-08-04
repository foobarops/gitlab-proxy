package com.example.gitlabproxy.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Component
public class GitLabGroupsApi {

	private static final String API_URL = "https://gitlab.com/api/v4";
	private String privateToken;

	public GitLabGroupsApi() {
		this.privateToken = null;
	}

	public List<Group> getGroups() throws IOException {
		String url = API_URL + "/groups?per_page=100&pagination=keyset&order_by=name";
        List<Group> result = new ArrayList<>();
        int cycles = 0;
        while (url != null && cycles++ < 2) {
                
            HttpURLConnection connection = createConnection(url);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                url = connection.getHeaderField("link").replaceFirst("^<", "").replaceFirst(">.*", "");
                // String nextPageUrl = extractNextPageUrl(linkHeader);
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                result.addAll(new Gson().fromJson(reader, new TypeToken<List<Group>>(){}.getType()));
            } else {
                throw new IOException("Failed to get groups: " + responseCode);
            }
        }
        return result;
	}

	public Group getGroup(int groupId) throws IOException {
		String url = API_URL + "/groups/" + groupId;
		HttpURLConnection connection = createConnection(url);
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			return new Gson().fromJson(reader, Group.class);
		} else {
			throw new IOException("Failed to get group: " + responseCode);
		}
	}

	private HttpURLConnection createConnection(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// connection.setRequestProperty("PRIVATE-TOKEN", privateToken);
		connection.setRequestProperty("Accept", "application/json");
		return connection;
	}

    @Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	public static class Group implements Serializable {
		@With private int id;
		@With private String name;
		@With private String path;
		@With private String fullPath;
	}
}
