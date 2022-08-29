package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.InvalidNameException;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CalendarControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);

    private final SprintRepository sprintRepository = mock(SprintRepository.class);

    private final EventRepository eventRepository = mock(EventRepository.class);

    private  final DeadlineRepository deadlineRepository = mock(DeadlineRepository.class);

    private final MilestoneRepository milestoneRepository = mock(MilestoneRepository.class);


    private final CalendarController calendarController = new CalendarController(projectRepository, sprintRepository, null, null, null);
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();


    private String joinParameters(HashMap<String, String> parameters) {
        String searchParams = "?";
        for (String key : parameters.keySet()) {
            searchParams += key + "=" + parameters.get(key) + "&";
        }
        return searchParams.substring(0, searchParams.length() - 1);
    }


    @BeforeEach
    public void beforeAll() {
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
        userBuilder.addRoles(UserRole.STUDENT);
        UserResponse user = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal, mockClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        when(mockClientService.getUserAccountById(userByIdRequest)).thenReturn(user);
        calendarController.setUserAccountsClientService(mockClientService);
        Project project = new Project("test");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    }

    @Test
    void testGetCalendar() {
        ModelAndView model = calendarController.getCalendar(new Authentication(principal), 1L);
        Assertions.assertEquals("monthlyCalendar", model.getViewName());

    }

    @Test
    void testGetCalendarWrongProjectId() {
        ModelAndView model = calendarController.getCalendar(new Authentication(principal), 2L);
        Assertions.assertEquals("errorPage", model.getViewName());
    }


    @Test
    void testGetProjectDetails() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectDetails").param("projectId", "1"));
        result.andExpect(status().isOk());
    }

    @Test
    void testGetProjectDetailsProjectDoesNotExist() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectDetails").param("projectId", "100"));
        result.andExpect(status().isNotFound());
    }

    @Test
    void testGetProjectSprintsWithDatesNoSprints() throws Exception {
        Project project = new Project("Testing");
        ZonedDateTime start = ZonedDateTime.now();
        ZonedDateTime end = ZonedDateTime.now();
        ResultActions result = this.mockMvc.perform(get("/getProjectSprintsWithDatesAsFeed")
                .param("projectId", project.getId().toString())
                .param("start", start.toString())
                .param("end", end.toString()));
        result.andExpectAll(status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON));
        String content = result.andReturn().getResponse().getContentAsString();
        Assertions.assertEquals("[]", content);

    }

    @Test
    void testGetProjectSprintsWithDatesWithSprints() {
        Project project = new Project("Testing");
        ZonedDateTime start = ZonedDateTime.now().minusMonths(1);
        ZonedDateTime end = ZonedDateTime.now().plusMonths(1);
        Sprint sprint = new Sprint(project, "TestSprint", LocalDate.now());
        List<Sprint> sprints = new ArrayList<>();
        sprints.add(sprint);
        when(sprintRepository.findAllByProjectId(project.getId())).thenReturn(sprints);
        ResponseEntity<Object> returnValue = calendarController.getProjectSprintsWithDates(project.getId(), start.toString(), end.toString());
        Assertions.assertEquals(HttpStatus.OK, returnValue.getStatusCode());
        Assertions.assertNotNull(returnValue.getBody());
        Assertions.assertTrue(returnValue.getBody().toString().contains("title=TestSprint"));
        Assertions.assertTrue(returnValue.getBody().toString().contains("start=" + sprint.getStartDate().toString()));

    }


    @Test
    void testGetProjectSprintsWithDatesWithBadDates() {
        Project project = new Project("Testing");
        ResponseEntity<Object> returnValue = calendarController.getProjectSprintsWithDates(project.getId(), "cheese", "grommit");
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, returnValue.getStatusCode());

    }

    @Test
    void testGetProjectSprintsWithDatesThrowsException() {
        Project project = new Project("Testing");
        ZonedDateTime start = ZonedDateTime.now().minusMonths(1);
        ZonedDateTime end = ZonedDateTime.now().plusMonths(1);

        when(sprintRepository.findAllByProjectId(project.getId())).thenThrow(new RuntimeException());
        ResponseEntity<Object> returnValue = calendarController.getProjectSprintsWithDates(project.getId(), start.toString(), end.toString());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, returnValue.getStatusCode());

    }

    @Test
    void testGetProjectSprintsWithDatesInsideOfSprintDates() {
        Project project = new Project("Testing");
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now().plusDays(1);
        Sprint sprint = new Sprint(project, "TestSprint", LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1));
        List<Sprint> sprints = new ArrayList<>();
        sprints.add(sprint);
        when(sprintRepository.findAllByProjectId(project.getId())).thenReturn(sprints);
        ResponseEntity<Object> returnValue = calendarController.getProjectSprintsWithDates(project.getId(), start.toString(), end.toString());
        Assertions.assertEquals(HttpStatus.OK, returnValue.getStatusCode());
        Assertions.assertNotNull(returnValue.getBody());
        Assertions.assertTrue(returnValue.getBody().toString().contains("title=TestSprint"));
        Assertions.assertTrue(returnValue.getBody().toString().contains("start=" + sprint.getStartDate().toString()));
        Assertions.assertTrue(returnValue.getBody().toString().contains("end="));

    }


    @Test
    void testGetProject() {
        Project project = new Project("Testing");
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        ResponseEntity<Object> returnValue = calendarController.getProject(project.getId());
        Assertions.assertEquals(HttpStatus.OK, returnValue.getStatusCode());
        Assertions.assertNotNull(returnValue.getBody());
        Assertions.assertTrue(returnValue.getBody().toString().contains("title=" + project.getName()));
        Assertions.assertTrue(returnValue.getBody().toString().contains("start=" + project.getStartDate()));


    }

    @Test
    void testGetProjectNotFound() {
        Project project = new Project("Testing");
        when(projectRepository.findById(project.getId())).thenThrow(new EntityNotFoundException());
        ResponseEntity<Object> returnValue = calendarController.getProject(project.getId());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, returnValue.getStatusCode());

    }

    @Test
    void testGetProjectException() {
        Project project = new Project("Testing");
        when(projectRepository.findById(project.getId())).thenThrow(new RuntimeException());
        ResponseEntity<Object> returnValue = calendarController.getProject(project.getId());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, returnValue.getStatusCode());

    }

    // ------- Events, Deadlines and Milestones tests ----------

    /**
     * A helper method that sets up some events, deadlines and milestones, as well as mocking them,
     * for the purposes of testing the getEventsAsFeed, getDeadlinesAsFeed, and getMilestonesAsFeed endpoints.
     */
    private void setUpEDM() {
        Project project = new Project("Testing");
        LocalDateTime startDate1 = LocalDateTime.of(2022, 2, 24, 12, 30);
        LocalDateTime endDate1 = LocalDateTime.of(2022, 3, 24, 12, 30);
        LocalDateTime startDate2 = LocalDateTime.of(2022, 4, 24, 12, 30);
        LocalDateTime endDate2 = LocalDateTime.of(2022, 5, 24, 12, 30);
        try {
            Event event1 = new Event(project, "Event1", startDate1, endDate1.toLocalDate(), endDate1.toLocalTime(), 1);
            Event event2 = new Event(project, "Event2", startDate2, endDate2.toLocalDate(), endDate2.toLocalTime(), 1);
            when(eventRepository.findAllByProjectIdOrderByStartDate(project.getId())).thenReturn(List.of(event1, event2));
        } catch (InvalidNameException e) {
            Assertions.fail("One of the events has an incorrect name!");
            //If this happens, our event has an incorrectly formatted name! This shouldn't occur in these tests
        }
        try {
            Deadline deadline1 = new Deadline(project, "Deadline1", endDate1.toLocalDate(), endDate1.toLocalTime(), 1);
            Deadline deadline2 = new Deadline(project, "Deadline2", endDate2.toLocalDate(), endDate2.toLocalTime(), 1);
            when(deadlineRepository.findAllByProjectIdOrderByEndDate(project.getId())).thenReturn(List.of(deadline1, deadline2));
        } catch (InvalidNameException e) {
            Assertions.fail("One of the Deadlines has an incorrect name!");
            //If this happens, our deadline has an incorrectly formatted name! This shouldn't occur in these tests
        }
        try {
            Milestone milestone1 = new Milestone(project, "Milestone1", endDate1.toLocalDate(), 1);
            Milestone milestone2 = new Milestone(project, "Milestone2", endDate2.toLocalDate(), 1);
            when(milestoneRepository.findAllByProjectIdOrderByEndDate(project.getId())).thenReturn(List.of(milestone1, milestone2));
        } catch (InvalidNameException e) {
            Assertions.fail("One of the Milestones has an incorrect name!");
            //If this happens, our milestone has an incorrectly formatted name! This shouldn't occur in these tests
        }
    }

    void testGetEventsAsFeed() {
        setUpEDM();

    }


}

