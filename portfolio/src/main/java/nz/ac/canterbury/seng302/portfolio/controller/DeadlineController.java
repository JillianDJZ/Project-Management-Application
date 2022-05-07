package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.InvalidNameException;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
public class DeadlineController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final ProjectRepository projectRepository;
    private final DeadlineRepository deadlineRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeadlineController(ProjectRepository projectRepository, DeadlineRepository deadlineRepository) {
        this.projectRepository = projectRepository;
        this.deadlineRepository = deadlineRepository;
    }

    /**
     * Mapping for a put request to add a deadline.
     * The method first parses a date and time string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format and a LocalTime format
     * <p>
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     * <p>
     * The deadline is then created with the parameters passed, and saved to the deadline repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param principal The AuthState of the user making the request, for authentication
     * @param projectId id of project to add deadline to.
     * @param name      Name of milestone.
     * @param dateEnd   date of the end of the deadline.
     * @param timeEnd   time of the end of the deadline
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addDeadline")
    public ResponseEntity<String> addDeadline(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineDateEnd") String dateEnd,
            @RequestParam(value = "deadlineTimeEnd") String timeEnd,
            @RequestParam(defaultValue = "1", value = "typeOfOccasion") int typeOfOccasion
    ) {
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has and if it's not a teacher or a course admin it returns a forbidden response
        List<UserRole> roles = userResponse.getRolesList();
        if (!roles.contains(UserRole.TEACHER) || !roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            if (name == null) {
                Long count = deadlineRepository.countDeadlineByProjectId(projectId);
                name = "Deadline " + (count + 1);
            } else if (name.length() > 50) {
                throw new InvalidNameException("The name of a deadline cannot be more than 50 characters");
            }

            LocalDate deadlineEndDate;
            LocalTime deadlineEndTime;
            if (dateEnd == null) {  // if the date is empty then set it as the start of the project
                deadlineEndDate = project.getEndDate();
            } else {
                deadlineEndDate = LocalDate.parse(dateEnd);
            }
            if (timeEnd == null){ // if the time is nothing then set it as midnight
                deadlineEndTime = LocalTime.MIN;
            } else {
                deadlineEndTime = LocalTime.parse(timeEnd);
            }
            if (typeOfOccasion < 1){
                throw new IllegalArgumentException("The type of the deadline is not a valid");
            }

            Deadline deadline = new Deadline(project, name, deadlineEndDate, deadlineEndTime, typeOfOccasion);
            deadlineRepository.save(deadline);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException | InvalidNameException | IllegalArgumentException err) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mapping for a post request to edit a deadline.
     * The method first gets the deadline from the repository. If the deadline cannot be retrieved, it throws an EntityNotFound exception.
     * <p>
     * The method then parses a date string and a time string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format.
     * <p>
     * The deadline is then edited with the parameters passed, and saved to the deadline repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param principal The AuthState of the user making the request, for authentication
     * @param deadlineId the ID of the deadline being edited.
     * @param projectId id of project to add deadline to.
     * @param name the new name of the deadline.
     * @param dateEnd the new date of the deadline.
     * @param timeEnd the new time of the deadline
     * @param typeOfOccasion the new type of the deadline.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editDeadline")
    public ResponseEntity editDeadline(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "deadlineId") UUID deadlineId,
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineDate") String dateEnd,
            @RequestParam(value = "deadlineTime") String timeEnd,
            @RequestParam(defaultValue = "1", value = "typeOfMilestone") int typeOfOccasion
    ) {
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has and if it's not a teacher or a course admin it returns a forbidden response
        List<UserRole> roles = userResponse.getRolesList();
        if (!roles.contains(UserRole.TEACHER) || !roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + " was not found"
            ));

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            // if no name is given then set a default one with "Deadline" followed by the next unused integer
            if (name == null) {
                Long count = deadlineRepository.countDeadlineByProjectId(projectId);
                name = "Deadline " + (count + 1);
            } else if (name.length() > 50) {
                throw new InvalidNameException("The name of a deadline cannot be more than 50 characters");
            }

            LocalDate deadlineEndDate;
            LocalTime deadlineEndTime;
            if (dateEnd == null) {  // if the date is empty then set it as the start of the project
                deadlineEndDate = project.getEndDate();
            } else {
                deadlineEndDate = LocalDate.parse(dateEnd);
            }
            if (timeEnd == null){ // if the time is nothing then set it as midnight
                deadlineEndTime = LocalTime.MIN;
            } else {
                deadlineEndTime = LocalTime.parse(timeEnd);
            }
            if (typeOfOccasion < 1){
                throw new IllegalArgumentException("The type of the deadline is not a valid");
            }

            deadline.setName(name);
            deadline.setEndDate(deadlineEndDate);
            deadline.setEndTime(deadlineEndTime);
            deadline.setType(typeOfOccasion);
            deadlineRepository.save(deadline);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException | InvalidNameException | IllegalArgumentException err) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mapping for deleting an existing deadline.
     * The method attempts to get the deadline from the repository and if it cannot it will throw an EntityNotFoundException
     *
     * Otherwise it will delete the deadline from the repository
     *
     * @param principal The AuthState of the user making the request, for authentication
     * @param deadlineId The UUID of the deadline to be deleted
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/deleteDeadline")
    public ResponseEntity editDeadline(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "deadlineId") UUID deadlineId) {
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has and if it's not a teacher or a course admin it returns a forbidden response
        List<UserRole> roles = userResponse.getRolesList();
        if (!roles.contains(UserRole.TEACHER) || !roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + " was not found"
            ));
            deadlineRepository.delete(deadline);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

}
