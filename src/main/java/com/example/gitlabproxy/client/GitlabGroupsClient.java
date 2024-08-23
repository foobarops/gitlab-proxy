package com.example.gitlabproxy.client;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
	
	private final GitlabGroupsCachingClient gitlabGroupsCachingClient;
	private final GitlabRetryableClient gitlabRetryableClient;

	private enum State { READY, FALLBACK, BULKHEAD };
	private State state = State.BULKHEAD;
	
	public void setStateReady() {
		state = State.READY;
		log.info("State changed to READY");
	}

	public List<Group> getGroups(String filter, boolean refresh) {
		if (state == State.BULKHEAD || refresh) {
			log.debug("Bulkhead mode or refresh requested");
			return gitlabRetryableClient.getGroups(filter, 100, 0);
		}
		
		return getFilteredGroups(filter).stream()
			.map(fullPath -> gitlabGroupsCachingClient.getGroup(fullPath))
			.filter(group -> group != null)
			.toList();
	}

	private List<String> getFilteredGroups(String filter) {
		synchronized (gitlabGroupsCachingClient.getGroups()) {
			return gitlabGroupsCachingClient.getGroups().stream()
				.filter(group -> filter == null || group.contains(filter)).toList();
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@ToString
	@Builder
	@EqualsAndHashCode
	public static class Group implements Serializable {
		private static final long serialVersionUID = 1223722373353637163L;
		@With private int id;
		@With private String name;
		@With private String path;
		@With @SerializedName("full_path") private String fullPath;
	}
}
