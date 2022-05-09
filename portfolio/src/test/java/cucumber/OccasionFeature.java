package cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.controller.DeadlineController;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.ResponseEntity;

import javax.naming.InvalidNameException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OccasionFeature {


    private DeadlineRepository deadlineRepository = new DeadlineRepository() {
        @Override
        public List<Deadline> findAllByProjectId(Long projectId) {
            return null;
        }

        @Override
        public Long countDeadlineByProjectId(Long projectId) {
            return (long) deadlines.size();
        }

        @Override
        public <S extends Deadline> S save(S entity) {
            deadlines.add(entity);
            return entity;
        }

        @Override
        public <S extends Deadline> Iterable<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<Deadline> findById(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(UUID uuid) {
            return false;
        }

        @Override
        public Iterable<Deadline> findAll() {
            return null;
        }

        @Override
        public Iterable<Deadline> findAllById(Iterable<UUID> uuids) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(UUID uuid) {

        }

        @Override
        public void delete(Deadline entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends UUID> uuids) {

        }

        @Override
        public void deleteAll(Iterable<? extends Deadline> entities) {

        }

        @Override
        public void deleteAll() {

        }
    };

    private static ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private static PrincipalAttributes mockPrincipal = mock(PrincipalAttributes.class);
    private static UserAccountsClientService clientService = mock(UserAccountsClientService.class);
    private AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();

    private ArrayList<Deadline> deadlines = new ArrayList<>();
    private Project project;
    private DeadlineController deadlineController = new DeadlineController(mockProjectRepository, deadlineRepository);
    private Milestone milestone;

    @Given("the user is authenticated: {string}")
    public void the_user_is_authenticated(String isAuthenticatedString) {
        deadlineController.setUserAccountsClientService(clientService);
        boolean isAuthenticated = Boolean.parseBoolean(isAuthenticatedString);
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.STUDENT);
        if (isAuthenticated){
            user.addRoles(UserRole.TEACHER);
        }
        when(PrincipalAttributes.getUserFromPrincipal(principal, clientService)).thenReturn(user.build());
    }

    @Given("a project exists from {string} to {string}")
    public void a_project_exists_from_start_date_to_end_date(String startDate, String endDate) {
        project = new Project("default", LocalDate.parse(startDate), LocalDate.parse(endDate), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
        Assertions.assertNotNull(project);
    }

    @When("the user creates a deadline for {string} with name {string}")
    public void a_user_creates_a_deadline_for_deadline_date_with_name_deadline_name(String deadlineDate, String deadlineName) {
        String date = null;
        String time = null;
        if (!deadlineDate.equals("left blank")) {
            LocalDateTime parsedDate = LocalDateTime.parse(deadlineDate);
            date = parsedDate.toLocalDate().toString();
            time = parsedDate.toLocalTime().toString();
        }
        if (deadlineName.equals("left blank")) {
            deadlineName = null;
        }
        ResponseEntity<String> stat = deadlineController.addDeadline(principal, project.getId(), deadlineName, date, time, 1);
    }

    @When("a user creates a milestone for {string} with name {string} and type {int}")
    public void a_user_creates_a_milestone_for_milestone_date_with_name_milestone_name(String milestoneDate, String milestoneName, int type) {
        if (milestoneDate.equals("left blank")) {
            milestoneDate = null;
        } else if (milestoneName.equals("left blank")) {
            milestoneName = null;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(milestoneDate);
            milestone = new Milestone(project, milestoneName, parsedDate, type);
        } catch (DateTimeException | NullPointerException | InvalidNameException e) {
            milestone = null;
        }
    }

    @Then("The deadline exists: {string}")
    public void the_deadline_exists(String deadlineExistsString) {
        boolean deadlineExists = Boolean.parseBoolean(deadlineExistsString);
        assertEquals(deadlineExists, deadlineRepository.countDeadlineByProjectId(project.getId()) == 1);
    }
    @Then("The milestone exists: {string}")
    public void the_milestone_exists(String milestoneExistsString) {
        boolean deadlineExists = Boolean.parseBoolean(milestoneExistsString);
        assertEquals(deadlineExists, milestone != null);
    }

}
