package com.example.gitlabproxy.controller;

import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.client.GitlabClient;
import com.example.gitlabproxy.service.GroupsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@MockBeans({
    @MockBean(GitlabClient.class),
})
@AutoConfigureMockMvc
@DisplayName("GroupsController tests")
class GroupsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupsService groupsService;

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(groupsService);
    }

    @Test
    @DisplayName("GET: Result of 2 elements")
    void getGroups() throws Exception {
        when(groupsService.getGroups(null, false)).thenReturn(
            GroupsWrapper.builder().groups(List.of(
                GroupsWrapper.Group.builder().fullPath("test/group1").build(),
                GroupsWrapper.Group.builder().fullPath("test/group2").build())).build()
        );
        this.mockMvc
            .perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"groups\": [{\"fullPath\":\"test/group1\"}, {\"fullPath\":\"test/group2\"}]}"));
        verify(groupsService).getGroups(null, false);
    }

    @Test
    @DisplayName("GET: Result with refresh")
    void getGroupsRefreshed() throws Exception {
        when(groupsService.getGroups(null, true)).thenReturn(
            GroupsWrapper.builder().groups(List.of(
                GroupsWrapper.Group.builder().fullPath("test/group1").build(),
                GroupsWrapper.Group.builder().fullPath("test/group2").build())).build()
        );
        this.mockMvc
            .perform(get("/groups").param("refresh", "true"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"groups\": [{\"fullPath\":\"test/group1\"}, {\"fullPath\":\"test/group2\"}]}"));
        verify(groupsService).getGroups(null, true);
    }

    @Test
    @DisplayName("GET: Empty result")
    void getGroupsEmptyResult() throws Exception {
        when(groupsService.getGroups(null, false)).thenReturn(
            GroupsWrapper.builder().groups(List.of()).build()
        );
        this.mockMvc
            .perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"groups\": []}"));
        verify(groupsService).getGroups(null, false);
    }

    @Test
    @DisplayName("GET: Filtered result")
    void getGroupsFiltered() throws Exception {
        when(groupsService.getGroups("group2", false)).thenReturn(
            GroupsWrapper.builder().groups(List.of(
                GroupsWrapper.Group.builder().fullPath("test/group2").build())).build()
        );
        this.mockMvc
            .perform(get("/groups").param("filter", "group2"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"groups\": [{\"fullPath\":\"test/group2\"}]}"));
        verify(groupsService).getGroups("group2", false);
    }

    @Test
    @DisplayName("GET: Filtered result with refresh")
    void getGroupsFilteredRefreshed() throws Exception {
        when(groupsService.getGroups("group2", true)).thenReturn(
            GroupsWrapper.builder().groups(List.of(
                GroupsWrapper.Group.builder().fullPath("test/group2").build())).build()
        );
        this.mockMvc
            .perform(get("/groups").param("filter", "group2").param("refresh", "true"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"groups\": [{\"fullPath\":\"test/group2\"}]}"));
        verify(groupsService).getGroups("group2", true);
    }

}