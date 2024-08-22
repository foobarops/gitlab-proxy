package com.example.gitlabproxy.client;

import java.util.TreeSet;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.gitlabproxy.client.GitlabGroupsClient.Group;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("client")
@Component
@RequiredArgsConstructor
public class GitlabGroupsCachingClient {
	
	@Getter
	private final TreeSet<String> groups = new TreeSet<>();

	@CachePut(value = "groupCache", key = "#group.fullPath")
	public Group putGroup(Group group) {
		groups.add(group.getFullPath());
		return group;
	}

	@Cacheable(value = "groupCache", key = "#fullPath")
	public Group getGroup(String fullPath) {
		return null;
	}
}
