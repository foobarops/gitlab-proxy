package com.example.gitlabproxy.controller;

import com.example.gitlabproxy.api.model.GroupsWrapper;
import com.example.gitlabproxy.service.GroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    @RequestMapping(method = RequestMethod.GET)
    public GroupsWrapper getGroups(
        @RequestParam(value = "refresh", required = false) boolean refresh,
        @RequestParam(value = "filter", required = false) String filter
        ) {
        if (filter != null) {
            // Perform filter logic here
            if (refresh) {
                // Perform refresh logic here
                return groupsService.getGroupsFiltered(filter, true);
            }
            return groupsService.getGroupsFiltered(filter);
        }
        if (refresh) {
            // Perform refresh logic here
            return groupsService.getGroups(true);
        }
        return groupsService.getGroups();
    }
}
