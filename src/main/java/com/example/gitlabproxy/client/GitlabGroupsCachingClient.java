package com.example.gitlabproxy.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
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
	private final Set<String> groups = Collections.synchronizedSet(new HashSet<>());

	@CachePut(value = "groupCache", key = "#group.fullPath")
	public Group putGroup(Group group) {
		synchronized (groups) {
			groups.add(group.getFullPath());
		}
		return group;
	}

	@Cacheable(value = "groupCache", key = "#fullPath", unless = "#result == null")
	public Group getGroup(String fullPath) {
		return null;
	}
}
