package com.example.gitlabproxy.controller;

import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.service.GroupsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Контроллер групп")
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
    @DisplayName("GET: Результат из 2-х элементов")
    void getGroups() throws Exception {
        when(groupsService.getGroups()).thenReturn(
            GroupsWrapper.builder().groups(List.of(
                GroupsWrapper.Group.builder().fullPath("test/group1").build(),
                GroupsWrapper.Group.builder().fullPath("test/group2").build())).build()
        );
        this.mockMvc
            .perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"groups\": [{\"fullPath\":\"test/group1\"}, {\"fullPath\":\"test/group2\"}]}"));
        verify(groupsService).getGroups();
    }

    @Test
    @DisplayName("GET: Пустой результат")
    void getGroupsEmptyResult() throws Exception {
        when(groupsService.getGroups()).thenReturn(
            GroupsWrapper.builder().groups(List.of()).build()
        );
        this.mockMvc
            .perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"groups\": []}"));
        verify(groupsService).getGroups();
    }

}