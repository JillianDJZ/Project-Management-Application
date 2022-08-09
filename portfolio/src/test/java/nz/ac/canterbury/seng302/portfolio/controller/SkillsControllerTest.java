package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SkillController.class)
@AutoConfigureMockMvc(addFilters = false)
class SkillsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication principal;

    @MockBean
    private AuthenticateClientService authenticateClientService;

    @MockBean
    private UserAccountsClientService userAccountsClientService;

    @MockBean
    private SkillRepository skillRepository;

    private final Integer validUserId = 1;
    private final Integer nonExistentUserId = 2;
    private final String invalidUserId = "Not an Id";


    @BeforeEach
    public void setup() {
        setUserToStudent();
        setUpContext();
    }


    @Test
    void testGetSkillsForUserWhenUserHasNoSkills() throws Exception {
        List<Skill> emptySkills = new ArrayList<>();
        String expectedResponseString = "[]";
        UserResponse validUserResponse = UserResponse.newBuilder().setId(validUserId).build();

        Mockito.when(skillRepository.findSkillsByEvidenceUserId(validUserId)).thenReturn(emptySkills);
        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(validUserResponse);

        MvcResult result = mockMvc.perform(get("/skills")
                .param("userId", String.valueOf(validUserId)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedResponseString, responseContent);
    }


    @Test
    void testGetSkillsForUserWhenUserHasOneSkill() throws Exception {
        Skill usersSkill = new Skill(1, "Skill 1");
        List<Skill> emptySkills = new ArrayList<>();
        emptySkills.add(usersSkill);
        String expectedResponseString = "[" + usersSkill.toJsonString() + "]";
        UserResponse validUserResponse = UserResponse.newBuilder().setId(validUserId).build();

        Mockito.when(skillRepository.findSkillsByEvidenceUserId(validUserId)).thenReturn(emptySkills);
        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(validUserResponse);

        MvcResult result = mockMvc.perform(get("/skills")
                        .param("userId", String.valueOf(validUserId)))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedResponseString, responseContent);
    }


    @Test
    void testGetSkillsForUserWhenUserHasMultipleSkills() throws Exception {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Skill usersSkill2 = new Skill(2, "Skill 2");
        Skill usersSkill3 = new Skill(3, "Skill 3");
        List<Skill> emptySkills = new ArrayList<>();
        emptySkills.add(usersSkill1);
        emptySkills.add(usersSkill2);
        emptySkills.add(usersSkill3);
        String expectedResponseString = "[" + usersSkill1.toJsonString() + "," + usersSkill2.toJsonString() +  "," + usersSkill3.toJsonString() + "]";
        UserResponse validUserResponse = UserResponse.newBuilder().setId(validUserId).build();

        Mockito.when(skillRepository.findSkillsByEvidenceUserId(validUserId)).thenReturn(emptySkills);
        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(validUserResponse);

        MvcResult result = mockMvc.perform(get("/skills")
                        .param("userId", String.valueOf(validUserId)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedResponseString, responseContent);
    }


    @Test
    void testGetSkillsReturnsBadRequestWhenUserNoUserIdIncluded() throws Exception {
        mockMvc.perform(get("/skills"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetSkillsReturnsNotFoundWhenUserNonExistentUserId() throws Exception {
        List<Skill> emptySkills = new ArrayList<>();
        UserResponse invalidUserResponse = UserResponse.newBuilder().setId(-1).build();

        Mockito.when(skillRepository.findSkillsByEvidenceUserId(validUserId)).thenReturn(emptySkills);
        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(invalidUserResponse);

        mockMvc.perform(get("/skills")
                .param("userId", String.valueOf(nonExistentUserId)))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetSkillsReturnsBadRequestWhenUserInvalidTypeUserId() throws Exception {
        mockMvc.perform(get("/skills")
                .param("userId", invalidUserId))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetEvidenceForSkillWhenSkillHasNoEvidence() throws Exception {
        Skill testSkill = new Skill(1, "writing_tests");

        Mockito.when(skillRepository.findByNameIgnoreCase(testSkill.getName())).thenReturn(Optional.of(testSkill));

        mockMvc.perform(get("/evidenceLinkedToSkill")
                .param("skillName", "writing_tests"))
                .andExpect(status().isOk());
    }


    @Test
    void testGetEvidenceForSkillWhenSkillHasOneEvidence() throws Exception {
        Skill testSkill = new Skill(1, "writing_tests");
        Evidence evidence1 = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        testSkill.getEvidence().add(evidence1);

        Mockito.when(skillRepository.findByNameIgnoreCase(testSkill.getName())).thenReturn(Optional.of(testSkill));

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToSkill")
                        .param("skillName", "writing_tests"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedContent = "[" + evidence1.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void testGetEvidenceForSkillWhenSkillHasMultipleEvidence() throws Exception {
        Skill testSkill = new Skill(1, "writing_tests");
        Evidence evidence1 = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        Evidence evidence2 = new Evidence(2, 2, "Title", LocalDate.now(), "description");
        Evidence evidence3 = new Evidence(3, 2, "Title", LocalDate.now(), "description");
        testSkill.getEvidence().add(evidence1);
        testSkill.getEvidence().add(evidence2);
        testSkill.getEvidence().add(evidence3);

        Mockito.when(skillRepository.findByNameIgnoreCase(testSkill.getName())).thenReturn(Optional.of(testSkill));

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToSkill")
                        .param("skillName", "writing_tests"))
                .andExpect(status().isOk())
                .andReturn();

        // Because the result is a set, we can't guarantee the order
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains(evidence1.toJsonString()));
        Assertions.assertTrue(responseContent.contains(evidence1.toJsonString()));
        Assertions.assertTrue(responseContent.contains(evidence1.toJsonString()));
    }


    @Test
    void testGetEvidenceForSkillWhenSkillDoesNotExist() throws Exception {
        mockMvc.perform(get("/evidenceLinkedToSkill")
                        .param("skillName", "Mystery"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetEvidenceForSkillWhenInternalErrorOccurs() throws Exception {
        Skill testSkill = new Skill(1, "writing_tests");
        RuntimeException e = new RuntimeException();
        Mockito.when(skillRepository.findByNameIgnoreCase(testSkill.getName())).thenThrow(e);

        mockMvc.perform(get("/evidenceLinkedToSkill")
                        .param("skillName", "writing_tests"))
                .andExpect(status().isInternalServerError());
    }


    // -------------- Helper context functions ----------------------------------------------------


    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }


    private void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.STUDENT);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userBuilder.build());
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

    }
}
