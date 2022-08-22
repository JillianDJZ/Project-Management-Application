package nz.ac.canterbury.seng302.portfolio.service;

import com.google.type.DateTime;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final SprintRepository sprintRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
    }

    public void updateTimeDeactivated(Long id, DateTime timeDeactivated) {
        projectRepository.deactivateProject(id, timeDeactivated);
    }

    /**
     * Gets the minimum start date for a project.
     * If the current start date is more than a year ago, returns that, otherwise returns exactly a year before today.
     *
     * @return LocalDate set a year in the past.
     */
    public LocalDate getMinStartDate(Project project) {
        if (project.getStartDate().isBefore(LocalDate.now().minusYears(1))) {
            return project.getStartDate();
        } else {
            return LocalDate.now().minusYears(1);
        }
    }

    /**
     * Gets the max start date for a project. This will be the start date of the first sprint, or LocalDate.MAX if the
     * project has no sprints.
     *
     * @return A LocalDate to be compared against
     */
    public LocalDate getMaxStartDate(Project project) {
        List<Sprint> sprintListStartDates = sprintRepository.getAllByProjectOrderByStartDateAsc(project);
        if (!sprintListStartDates.isEmpty()) {
            Sprint sprint = sprintListStartDates.get(0);
            return sprint.getStartDate();
        } else {
            return LocalDate.MAX;
        }
    }


    /**
     * Gets the minimum date that the project end date can be set to. Will either be the end date of the last sprint,
     * or if no sprints the project start date.
     *
     * @return A LocalDate to be compared against
     */
    public LocalDate getMinEndDate(Project project) {
        List<Sprint> sprintListEndDates = sprintRepository.getAllByProjectOrderByEndDateDesc(project);
        if (!sprintListEndDates.isEmpty()) {
            Sprint sprint = sprintListEndDates.get(0);
            return sprint.getEndDate();
        } else {
            return project.getStartDate();
        }
    }


}
