package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.demodata.DataInitialisationManagerPortfolio;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Project project;


    private final AuthenticateClientService authenticateClientService  = mock(AuthenticateClientService.class);
    private final UserAccountsClientService userAccountsClientService = mock(UserAccountsClientService.class);
    private final GroupsClientService groupsClientService  = mock(GroupsClientService.class);
    private final SprintRepository sprintRepository = mock(SprintRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final RegexService regexService = spy(RegexService.class);
    @MockBean
    private SkillRepository skillRepository;

    @MockBean
    private EvidenceRepository evidenceRepository;

    @MockBean
    private DataInitialisationManagerPortfolio dataInitialisationManagerPortfolio;

    private Authentication principal = new Authentication(
            AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build()
    );

    @InjectMocks
    private final PortfolioController portfolioController = new PortfolioController(sprintRepository,projectRepository,userAccountsClientService, regexService);

    private final Integer validUserId = 1;
    private final Integer nonExistentUserId = 2;
    private final String invalidUserId = "Not an Id";


    @BeforeEach
    public void setup() {
        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");


        project = new Project("Project Seng302",
                LocalDate.parse("2022-02-25"),
                LocalDate.parse("2022-09-30"),
                "SENG302 is all about putting all that you have learnt in" +
                        " other courses into a systematic development process to" +
                        " create software as a team.");



        userBuilder.addRoles(UserRole.TEACHER);
        UserResponse user = userBuilder.build();
        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        when(userAccountsClientService.getUserAccountById(userByIdRequest)).thenReturn(user);
        UserRegisterResponse userRegisterResponse = UserRegisterResponse.newBuilder().setIsSuccess(true).build();
        when(userAccountsClientService.register(any(UserRegisterRequest.class))).thenReturn(userRegisterResponse);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.of(project));


    }



    @Test
    void testGetPortfolio(){
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("portfolio", modelAndView.getViewName());
    }


    @Test
    void testGetPortfolioNoProject() {
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }


    @Test
    void testGetPortfolioThrowsException() {
        when(projectRepository.findById(Mockito.any())).thenReturn(null);
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }

    @Test
    void testGetPortfolioRolesAreStudent(){
        setUserToStudent();
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("portfolio", modelAndView.getViewName());
        Assertions.assertEquals(false, modelAndView.getModel().get("userCanEdit"));
    }

    @Test
    void testGetPortfolioRolesAreTeacherOrAbove(){
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("portfolio", modelAndView.getViewName());
        Assertions.assertEquals(true, modelAndView.getModel().get("userCanEdit"));
    }

    @Test
    void testGetEditProjectPage(){
        ModelAndView modelAndView = portfolioController.edit(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("projectEdit", modelAndView.getViewName());
    }

    @Test
    void testGetEditProjectPageNoProject(){
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ModelAndView modelAndView = portfolioController.edit(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }

    @Test
    void testGetEditProjectPageThrowsException(){
        when(projectRepository.findById(Mockito.any())).thenReturn(null);
        ModelAndView modelAndView = portfolioController.edit(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }

    @Test
    void testEditProject(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testEditProjectNoProject(){
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void testEditProjectThrowsException(){
        when(projectRepository.findById(Mockito.any())).thenReturn(null);
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().isError());
    }

    @Test
    void testEditProjectNameTooLong(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name".repeat(400), LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Project name is longer than the maximum length", response.getBody());
    }

    @Test
    void testEditProjectNameTooShort(){
        ProjectRequest projectRequest = new ProjectRequest("1", "", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Project name is shorter than the minimum length", response.getBody());
    }

    @Test
    void testEditProjectDescriptionTooLong(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description".repeat(400));
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Project description is longer than the maximum length", response.getBody());
    }


    @Test
    void testEditProjectNewStartDateToFarInPast(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().minusYears(2).toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Project cannot start more than a year before its original date", response.getBody());
    }


    @Test
    void testEditProjectNewEndDateIsBeforeSprintsEnd(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().plusDays(1).toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        when(sprintRepository.findAllByProjectId(Mockito.any())).thenReturn(getSprints());
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("There is a sprint that falls after these new dates", response.getBody());
    }


    @Test
    void testEditProjectNewStartDateIsAfterSprintsStart(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().plusMonths(4).toString(), LocalDate.now().plusMonths(4).plusDays(4).toString(), "New Description");
        when(sprintRepository.findAllByProjectId(Mockito.any())).thenReturn(getSprints());
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("There is a sprint that falls before these new dates", response.getBody());
    }

    @Test
    void testEditProjectNewStartDateBeforeNewEndDate(){
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().minusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("End date cannot be before start date", response.getBody());
    }

    @Test
    void testAddSprint(){
        ResponseEntity<Object> response = portfolioController.addSprint(project.getId());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testAddSprintNoMoreRoom(){
        project = new Project("Project Seng302",
                LocalDate.now(),
                LocalDate.now().plusWeeks(2),
                "SENG302 is all about putting all that you have learnt in" +
                        " other courses into a systematic development process to" +
                        " create software as a team.");
        ArrayList<Sprint> sprints = new ArrayList<>();
        Sprint sprint = new Sprint(project, "test", LocalDate.now());
        sprints.add(sprint);
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(project));
        when(sprintRepository.findAllByProjectId(Mockito.anyLong())).thenReturn(sprints);
        ResponseEntity<Object> response = portfolioController.addSprint(project.getId());
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("No more room to add sprints within project dates!", response.getBody());
    }

    // -------------- Helper context functions ----------------------------------------------------

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


    private ArrayList<Sprint> getSprints() {
        Sprint sprint = new Sprint(project, "test", LocalDate.now().plusWeeks(1));
        Sprint sprint2 = new Sprint(project,"test2", LocalDate.now().plusWeeks(1).plusMonths(1));
        ArrayList<Sprint> arrayList = new ArrayList<>();
        arrayList.add(sprint);
        arrayList.add(sprint2);
        return arrayList;
    }



}
