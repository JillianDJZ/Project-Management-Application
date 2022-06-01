package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.apache.catalina.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = GroupsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GroupsControllerTest {

    private AuthState principal;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    GroupsClientService groupsClientService;

    @MockBean
    AuthenticateClientService authenticateClientService;


    @Test
    void testDeleteGroupUnauthorizedToStudent() throws Exception {
        setUserToStudent();
        setUpContext();

        mockMvc.perform(delete("/groups/edit"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testCreateGroupUnauthorizedToStudent() throws Exception {
        setUserToStudent();
        setUpContext();

        mockMvc.perform(post("/groups/edit"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testCreateValidShortAndLongName() throws Exception {
        setUserToTeacher();
        setUpContext();
        String shortName = "Test Name";
        String longName = "Test Name But Longer";
        CreateGroupRequest request = buildCreateRequest(shortName, longName);
        CreateGroupResponse response = CreateGroupResponse.newBuilder()
                                                          .setIsSuccess(true)
                                                          .setNewGroupId(3)
                                                          .build();
        Mockito.when(groupsClientService.createGroup(request)).thenReturn(response);

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isCreated());
    }



    @Test
    void testCreateGroupInvalidShortName() throws Exception {
        setUserToTeacher();
        setUpContext();
        String shortName = "Test Name";
        String longName = "Test Name But Longer";
        CreateGroupRequest request = buildCreateRequest(shortName, longName);

        CreateGroupResponse response = CreateGroupResponse.newBuilder()
                .addValidationErrors(ValidationError.newBuilder()
                        .setFieldName("Short name")
                        .setErrorText("A group exists with the shortName " + request.getShortName())
                        .build())
                .setIsSuccess(false)
                .build();

        Mockito.when(groupsClientService.createGroup(request)).thenReturn(response);

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testCreateGroupInvalidLongName() throws Exception {
        setUserToTeacher();
        setUpContext();
        String shortName = "Test Name";
        String longName = "Test Name But Longer";
        CreateGroupRequest request = buildCreateRequest(shortName, longName);

        CreateGroupResponse response = CreateGroupResponse.newBuilder()
                .addValidationErrors(ValidationError.newBuilder()
                        .setFieldName("Long name")
                        .setErrorText("A group exists with the longName " + request.getLongName())
                        .build())
                .setIsSuccess(false)
                .build();

        Mockito.when(groupsClientService.createGroup(request)).thenReturn(response);

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testDeleteGroupValid() throws Exception {
        setUserToTeacher();
        setUpContext();
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder().setGroupId(1).build();

        DeleteGroupResponse response = DeleteGroupResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Successfully deleted the group with Id: " + request.getGroupId())
                .build();

        Mockito.when(groupsClientService.deleteGroup(request)).thenReturn(response);

        mockMvc.perform(delete("/groups/edit")
                        .param("groupId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteGroupInvalid() throws Exception {
        setUserToTeacher();
        setUpContext();
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder().setGroupId(1).build();

        DeleteGroupResponse response = DeleteGroupResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("No group exists with Id: " + request.getGroupId())
                .build();

        Mockito.when(groupsClientService.deleteGroup(request)).thenReturn(response);

        mockMvc.perform(delete("/groups/edit")
                        .param("groupId", "1"))
                .andExpect(status().isNotFound());
    }



    @Test
    void addUsersToGroup() throws Exception { //fixMe I fail
        setUserToTeacher();
        setUpContext();

        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));

        }
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder().setGroupId(Integer.parseInt(groupId)).addAllUserIds(userIds).build();

        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Successfully added users to group")
                .build();

        Mockito.when(groupsClientService.addGroupMembers(request)).thenReturn(response);

        mockMvc.perform(post("/groups/addUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isOk());
    }

    @Test
    void removeUsers() throws Exception {
        setUserToTeacher();
        setUpContext();
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder().setGroupId(Integer.parseInt(groupId)).addAllUserIds(userIds).build();

        RemoveGroupMembersResponse response = RemoveGroupMembersResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Successfully removed users from group")
                .build();

        Mockito.when(groupsClientService.removeGroupMembers(request)).thenReturn(response);

        mockMvc.perform(delete("/groups/removeUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isOk());
    }


    @Test
    void addUsersToGroupNotAGroup() throws Exception {
        setUserToTeacher();
        setUpContext();
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder().setGroupId(Integer.parseInt(groupId)).addAllUserIds(userIds).build();

        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage(groupId + " does not refer to a valid group")
                .build();

        Mockito.when(groupsClientService.addGroupMembers(request)).thenReturn(response);

        mockMvc.perform(post("/groups/addUsers")
                        .param("groupId", groupId)
                        .params(params))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void addUserToGroupNotUser() throws Exception { //fIXme succeeding for the wrong reasons
        setUserToTeacher();
        setUpContext();
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder().setGroupId(Integer.parseInt(groupId)).addAllUserIds(userIds).build();

        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("1 does not refer to a valid user")
                .build();

        Mockito.when(groupsClientService.addGroupMembers(request)).thenReturn(response);

        mockMvc.perform(post("/groups/addUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteUserNotAGroup() throws Exception {
        setUserToTeacher();
        setUpContext();
        String groupId = "100";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder().setGroupId(Integer.parseInt(groupId)).addAllUserIds(userIds).build();

        RemoveGroupMembersResponse response = RemoveGroupMembersResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage(groupId + " does not refer to a valid group")
                .build();

        Mockito.when(groupsClientService.removeGroupMembers(request)).thenReturn(response);

        mockMvc.perform(delete("/groups/removeUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isBadRequest());
    }



    // ------------------------------------- Helpers -----------------------------------------

    private CreateGroupRequest buildCreateRequest(String shortName, String longName) {
        return CreateGroupRequest.newBuilder()
                .setShortName(shortName)
                .setLongName(longName)
                .build();
    }


    private void setUserToStudent() {
        principal = AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build();
    }

    private void setUserToTeacher() {
        principal = AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("course_administrator").build())
                .build();
    }

    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal);

        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }
}