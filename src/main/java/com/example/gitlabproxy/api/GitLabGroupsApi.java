package com.example.gitlabproxy.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
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

	public List<Group> getGroups() throws IOException {
		String url = apiUrl + "/groups?per_page=100&pagination=keyset&order_by=name";
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
		String url = apiUrl + "/groups/" + groupId;
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
		URL url;
		try {
			url = new URI(urlString).toURL();
		} catch (URISyntaxException e) {
			throw new MalformedURLException("Failed to create connection: " + e.getMessage());
		}
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		if (privateToken != null) {
			connection.setRequestProperty("PRIVATE-TOKEN", privateToken);
		}
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
