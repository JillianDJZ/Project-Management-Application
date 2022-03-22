package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.EventRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.events.Event;
import nz.ac.canterbury.seng302.portfolio.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;


import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@RestController
public class PortfolioController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;

    //Selectors for the error/info/success boxes.
    private final String errorMessage = "errorMessage";
    private final String infoMessage = "infoMessage";
    private final String successMessage = "successMessage";

    //below is for testing purposes
    private long projectId;





    /**
     * Constructor for PortfolioController
     * @param sprintRepository repository
     * @param projectRepository repository
     */
    public PortfolioController(SprintRepository sprintRepository, ProjectRepository projectRepository, EventRepository eventRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;

        //Below are only for testing purposes.
        this.projectId = createDefaultProject();
        createDefaultEvents(this.projectId);
        createDefaultSprints(this.projectId);
    }





    //Testing purposes
    public long createDefaultProject(){
        Project project = projectRepository.save(new Project(1, "Project Default"));
        return project.getId();

    }

    public void createDefaultEvents(long projectId) {
        LocalDate date = LocalDate.now();
        Event event1 = new Event(projectId, "Sprint1 to Sprint2", date, date.plusWeeks(4));
        Event event2 = new Event(projectId, "Sprint1 to Sprint4", date, date.plusWeeks(12));
        Event event3 = new Event(projectId, "Merry Chrysler Day", date.minusDays(10), date.plusDays(20));
        Event event4 = new Event(projectId, "Not in a sprint - Sprint 6", date.plusWeeks(19), date.plusWeeks(21));
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        eventRepository.save(event4);
    }

    public void createDefaultSprints(long projectId) {
        LocalDate date = LocalDate.now();
        Sprint sprint1 = new Sprint(projectId, "Sprint 1", date, date.plusWeeks(3), "Default", "#ef476f");
        Sprint sprint2 = new Sprint(projectId, "Sprint 2", date.plusWeeks(3).plusDays(1), date.plusWeeks(6), "Default", "#ffd166");
        Sprint sprint3 = new Sprint(projectId, "Sprint 3", date.plusWeeks(6).plusDays(1), date.plusWeeks(9), "Default", "#06d6a0");
        Sprint sprint4 = new Sprint(projectId, "Sprint 4", date.plusWeeks(9).plusDays(1), date.plusWeeks(12), "Default", "#118ab2");
        Sprint sprint5 = new Sprint(projectId, "Sprint 5", date.plusWeeks(12).plusDays(1), date.plusWeeks(15), "Default", "#219ebc");
        Sprint sprint6 = new Sprint(projectId, "Sprint 6", date.plusWeeks(20).plusDays(1), date.plusWeeks(22), "Default", "#f48c06");
        sprintRepository.save(sprint1);
        sprintRepository.save(sprint2);
        sprintRepository.save(sprint3);
        sprintRepository.save(sprint4);
        sprintRepository.save(sprint5);
        sprintRepository.save(sprint6);
    }







    /**
     * Main entry point for portfolio.
     *
     * @param principal The authentication state
     * @return Thymeleaf template
     */
    @GetMapping("/portfolio")
    public ModelAndView getPortfolio(
                                  @AuthenticationPrincipal AuthState principal
    ) {





        // Get user from server
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        ModelAndView modelAndView = new ModelAndView("portfolio");
        //TODO Change the below line so that it isn't just grabbing one single project?.
        Project project = projectRepository.getProjectById(projectId);

        addModelAttributeProject(modelAndView, project, user);



        List<Event> eventList = eventRepository.findAllByProjectIdOrderByStartDate(project.getId());
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(project.getId());
        for(Event event: eventList) {
            for (Sprint sprint: sprintList) {
                LocalDate eStart = event.getStartDate();
                LocalDate eEnd = event.getEndDate();
                LocalDate sStart = sprint.getStartDate();
                LocalDate sEnd = sprint.getEndDate();
                if ((eStart.isAfter(sStart) || eStart.isEqual(sStart)) && (eStart.isBefore(sEnd) || eStart.isEqual(sEnd))){
                    //Event start date is between or equal to sprint start and end dates.
                    event.setStartDateColour(sprint.getColour());
                    if(!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
                if ((eEnd.isAfter(sStart) || eEnd.isEqual(sStart)) && (eEnd.isBefore(sEnd) || eEnd.isEqual(sEnd))){
                    //Event end date is between or equal to sprint start and end dates.
                    event.setEndDateColour(sprint.getColour());
                    if(!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
                if (eStart.isBefore(sStart) && eEnd.isAfter(sEnd)) {
                    //Event spans over the entire sprint
                    if(!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
            }
        }




        modelAndView.addObject("project", project);
        modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
        modelAndView.addObject("events", eventList);
        return modelAndView;
    }

    /**
     * Mapping for /editProject
     * Retrieves the Project from the project repository by the id passed in with request parameters.
     * Calls helper function and returns thymeleaf template.
     *
     * @param principal The authentication state
     * @param projectId Id of project
     * @return a thymeleaf template
     */
    @RequestMapping("/editProject")
    public ModelAndView edit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "projectId") String projectId
    ) {
        // Get user from server
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        Long longProjectId = Long.parseLong(projectId);
        Project project = projectRepository.getProjectById(longProjectId);
        ModelAndView modelAndView = new ModelAndView("projectEdit");
        addModelAttributeProject(modelAndView, project, user);
        return modelAndView;
    }


    /**
     * Mapping for /projectEdit
     * Called when user has edited a project and hit submit.
     * Gets the correct project and updates all the information and redirects user back to main page
     * @param editInfo the thymeleaf-created form object
     * @return a redirect to portfolio
     */
    @PostMapping("/projectEdit")
    public ModelAndView editDetails(
            @ModelAttribute(name="editProjectForm") ProjectRequest editInfo,
            RedirectAttributes attributes
    ) {
        try {
            LocalDate projectStart = LocalDate.parse(editInfo.getProjectStartDate());
            LocalDate projectEnd = LocalDate.parse(editInfo.getProjectEndDate());
            Project project = projectRepository.getProjectById(Long.parseLong(editInfo.getProjectId()));
            project.setName(editInfo.getProjectName());
            project.setStartDate(projectStart);
            project.setEndDate(projectEnd);
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);
            attributes.addFlashAttribute(successMessage, "Project Updated!");
        } catch(Exception err) {
           attributes.addFlashAttribute(errorMessage, err);
        }
        return new ModelAndView("redirect:/portfolio");
    }


    /**
     * Helper function to add objects to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param project The project
     * @param model The model you're adding attributes to
     */
    public void addModelAttributeProject(ModelAndView model, Project project, UserResponse user){
        model.addObject("projectId", project.getId());
        model.addObject("projectName", project.getName());
        model.addObject("projectStart", project.getStartDate());
        model.addObject("projectEnd", project.getEndDate());
        model.addObject("projectDescription", project.getDescription());
        model.addObject("username", user.getUsername());
    }



    /**
     * Mapping for POST request "addSprint"
     * @param projectId the project in which you want to add sprint too.
     * @return Returns JSON of Sprint Object
     */
    @GetMapping("/portfolio/addSprint")
    public ModelAndView addSprint(
            @RequestParam (value = "projectId") String projectId,
            RedirectAttributes attributes)  {
        try {
            long longProjectId = Long.parseLong(projectId);
            int amountOfSprints = sprintRepository.findAllByProjectId(longProjectId).size() + 1;
            String sprintName = "Sprint " + amountOfSprints;
            LocalDate startDate;

            //If there are sprints in the repository, start date of added sprint is after the last sprints end date.
            if (sprintRepository.count() > 0) {
                Iterable<Sprint> sprints = sprintRepository.findAll();
                LocalDate prevSprintEndDate = null;
                for (Sprint sprint:sprints) {
                    if (prevSprintEndDate == null){
                        prevSprintEndDate = sprint.getEndDate();
                    } else {
                        if (sprint.getEndDate().isAfter(prevSprintEndDate)) {
                            prevSprintEndDate = sprint.getEndDate();
                        }
                    }
                }
                assert prevSprintEndDate != null;
                startDate = prevSprintEndDate.plusDays(1);
            } else {
                startDate = LocalDate.now();
            }
            sprintRepository.save(new Sprint(longProjectId, sprintName, startDate));
            attributes.addFlashAttribute(successMessage, "Sprint added!");
        } catch(Exception err) {
            attributes.addFlashAttribute(errorMessage, err);
        }

        return new ModelAndView("redirect:/portfolio");
    }

    /**
     * Mapping for /sprintEdit. Looks for a sprint that matches the id
     * and then populates the form.
     * @param principal The authentication state
     * @param sprintId The sprint id
     * @return Thymleaf template
     */
    @RequestMapping("/sprintEdit")
    public ModelAndView sprintEdit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "sprintId") String sprintId,
            RedirectAttributes attributes
    ) {
        // Get user from server
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        try {
            UUID uuidSprintId = UUID.fromString(sprintId);
            ModelAndView modelAndView = new ModelAndView("sprintEdit");
            addModelAttributeSprint(modelAndView, sprintRepository.getSprintById(uuidSprintId), user);
            return modelAndView;
        } catch(Exception err) {
            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("redirect:/portfolio");
        }


    }

    /**
     * Takes the request to update the sprint.
     * Tries to update the sprint then redirects user.
     * @param sprintInfo the thymeleaf-created form object
     * @return redirect to portfolio
     */
    @PostMapping("/sprintSubmit")
    public ModelAndView updateSprint(
            RedirectAttributes attributes,
            @ModelAttribute(name="sprintEditForm") SprintRequest sprintInfo) {

        try {
            LocalDate sprintStart = LocalDate.parse(sprintInfo.getSprintStartDate());
            LocalDate sprintEnd = LocalDate.parse(sprintInfo.getSprintEndDate());
            if (sprintStart.isAfter(sprintEnd)) {
                String dateErrorMessage = "Start date needs to be before end date";
                attributes.addFlashAttribute(errorMessage, dateErrorMessage);
            } else {
                Sprint sprint = sprintRepository.getSprintById(sprintInfo.getSprintId());
                sprint.setName(sprintInfo.getSprintName());
                sprint.setStartDate(sprintStart);
                sprint.setEndDate(sprintEnd);
                sprint.setDescription(sprintInfo.getSprintDescription());
                sprint.setColour(sprintInfo.getSprintColour());
                sprintRepository.save(sprint);
                return new ModelAndView("redirect:/portfolio");
            }
            return new ModelAndView("redirect:/sprintEdit?sprintId=" + sprintInfo.getSprintId());

        } catch(Exception err) {

            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("redirect:/sprintEdit?sprintId=" + sprintInfo.getSprintId());
        }
    }

    /**
     * Helper function to add objects to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param sprint The sprint
     * @param model The model you're adding attributes to
     */
    public void addModelAttributeSprint(ModelAndView model, Sprint sprint, UserResponse user){
        model.addObject("sprintId", sprint.getId());
        model.addObject("sprintName", sprint.getName());
        model.addObject("sprintStart", sprint.getStartDate());
        model.addObject("sprintEnd", sprint.getEndDate());
        model.addObject("sprintDescription", sprint.getDescription());
        model.addObject("sprintColour", sprint.getColour());
        model.addObject("username", user.getUsername());
    }


    /**
     * Mapping for PUT request "deleteSprint"
     * @param id UUID of sprint to delete
     * @return Confirmation of delete
     */
   @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam (value = "sprintId")UUID id) {
        sprintRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
   }






//    @GetMapping("addEvent")
//    public ModelAndView addEvent(
//            @ModelAttribute EventRequest eventRequest,
//            RedirectAttributes attributes) {
//        try {
//            Event event = new Event(eventRequest.getProjectId(), eventRequest.getEventName(),
//                    eventRequest.getEventStartDate(),
//                    eventRequest.getEventEndDate());
//            eventRepository.save(event);
//            attributes.addFlashAttribute("successMessage", "Event added!");
//        } catch(Exception err) {
//            attributes.addFlashAttribute("errorMessage", err);
//        }
//
//        return new ModelAndView("redirect:/portfolio");
//
//    }



}
