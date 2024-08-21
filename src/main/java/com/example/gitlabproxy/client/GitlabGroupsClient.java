package com.example.gitlabproxy.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
	
	private final TreeSet<String> groups = new TreeSet<>();
	private enum State { READY, FALLBACK, BULKHEAD };
	private State state = State.BULKHEAD;
	
	public void setStateReady() {
		state = State.READY;
	}

	@CachePut(value = "groupCache", key = "#group.fullPath")
	public Group putGroup(Group group) {
		groups.add(group.getFullPath());
		return group;
	}

	@Cacheable(value = "groupCache", key = "#fullPath")
	public Group getGroup(String fullPath) {
		return null;
	}

	// TODO implement bulkhead mode
	public Collection<String> getGroups(boolean refresh) {
		if (state == State.BULKHEAD) {
			log.error("Groups not yet initialized");
			throw new IllegalStateException("Groups not yet initialized and bulhead mode not implemented");
		}
		return groups;
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
