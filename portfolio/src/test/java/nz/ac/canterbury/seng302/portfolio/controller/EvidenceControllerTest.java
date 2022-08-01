package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = EvidenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class EvidenceControllerTest {

    private Authentication principal;


    @Autowired
    private MockMvc mockMvc;

    private UserResponse userResponse;

    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private final AuthState authState = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();

    @MockBean
    AuthenticateClientService authenticateClientService;

    @MockBean
    UserAccountsClientService userAccountsClientService;

    @MockBean
    EvidenceRepository evidenceRepository;

    @MockBean
    ProjectRepository projectRepository;

    @InjectMocks
    EvidenceService evidenceService = Mockito.spy(EvidenceService.class);


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testAddEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isOk());
    }

    @Test
    void testAddEvidenceOutsideProjectDates() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().minusDays(1).toString();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }

    @Test
    void testAddEvidenceTitleEmpty() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }

    @Test
    void testAddEvidenceTitleMixed() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "@#!@#&(*&!@#(&*!@(*&#(*!@&#(&(*&!@(*#&!@#asdasd";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isOk());
    }

    @Test
    void testAddEvidenceTitleLength() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "This should almost definitely be past 50 characters in length?";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }

    @Test
    void testAddEvidenceTitleNoAlpha() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "@@@";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionEmpty() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "testing";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }

    @Test
    void testAddEvidenceDescriptionMixed() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "@#!@#&(*&!@#(&*!@(*&#(*!@&#(&(*&!@(*#&!@#asdasd";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isOk());
    }

    @Test
    void testAddEvidenceDescriptionLength() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "This should almost definitely be past 500 characters in length?                                                                                                                                                                                                                                                                      This should almost definitely be past 500 characters in length?                                                                                                                                                                                                                                                                      ";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }

    @Test
    void testAddEvidenceDescriptionNoAlpha() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "Test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "@@@";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescription() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "title";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "Description";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isOk());

    }


    @Test
    void testAddEvidenceProjectId() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDate() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "WOW this shouldn't work";
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }

    @Test
    void testAddEvidenceDateNoDate() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "";
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());

    }

    @Test
    void testAddEvidenceException() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "WOW this shouldn't work";
        String description = "testing";
        long projectId = 1;
        Mockito.when(projectRepository.findById(projectId)).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isInternalServerError());
    }


    // ------------------------------------ GET evidence tests -------------------------------------

    @Test
    void TestGetEvidenceWhenUserExistsAndHasNoEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdOrderByDateDesc(1)).thenReturn(new ArrayList<>());

        MvcResult result = mockMvc.perform(get("/evidenceData")
                .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserExistsAndHasOneEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        usersEvidence.add(evidence);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        String expectedContent = "[" + evidence.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserExistsAndHasMultipleEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence1 = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        Evidence evidence2 = new Evidence(3, 4, "Title 2", LocalDate.now(), "description 2");
        usersEvidence.add(evidence1);
        usersEvidence.add(evidence2);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andReturn();

        String expectedContent = "[" + evidence1.toJsonString() + "," + evidence2.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserDoesntExistsReturnsStatusNotFound() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String notExistingUserId = "2";

        mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", notExistingUserId))
                .andExpect(status().isNotFound());
    }


    @Test
    void TestGetEvidenceWhenBadUserIdReturnsStatusBadRequest() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String illegalUserId = "IllegalId";

        mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", illegalUserId))
                .andExpect(status().isBadRequest());
    }


    @Test
    void TestGetEvidenceReturnsBadRequestWhenNoIdIncluded() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();

        mockMvc.perform(get("/evidenceData"))
                .andExpect(status().isBadRequest());
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
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

    }


    private void initialiseGetRequestMocks() {
        GetUserByIdRequest existingUserRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        UserResponse userResponse = UserResponse.newBuilder().setId(1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(existingUserRequest)).thenReturn(userResponse);

        GetUserByIdRequest nonExistentUserRequest = GetUserByIdRequest.newBuilder().setId(2).build();
        UserResponse notFoundResponse = UserResponse.newBuilder().setId(-1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(nonExistentUserRequest)).thenReturn(notFoundResponse);
    }
}
