package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.DTO.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.evidence.*;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import org.springframework.beans.factory.annotation.Autowired;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;


/**
 * Used to differentiate the strings that are passed to the stringCheck method
 */
enum StringType {
    TITLE,
    DESCRIPTION,
}

/**
 * A utility class for more complex actions involving Evidence
 */
@Service
public class EvidenceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String stringRegex = "[a-zA-Z0-9\s]*";

    private final UserAccountsClientService userAccountsClientService;

    private final ProjectRepository projectRepository;

    private final EvidenceRepository evidenceRepository;

    private final WebLinkRepository webLinkRepository;

    private final SkillRepository skillRepository;

    @Autowired
    public EvidenceService(
            UserAccountsClientService userAccountsClientService,
            ProjectRepository projectRepository,
            EvidenceRepository evidenceRepository,
            WebLinkRepository webLinkRepository,
            SkillRepository skillRepository
    ) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
        this.webLinkRepository = webLinkRepository;
        this.skillRepository = skillRepository;
    }


    /**
     * Checks if the string is too short or matches the pattern provided
     * if either of these are true then it throws an exception
     *
     * @param string A string
     * @throws CheckException The exception to throw
     */
    private void checkString(String string, StringType type) throws CheckException {
        if (string.length() < 2) {
            throw new CheckException("Text should be longer than 1 character");
        } else if (!string.matches(stringRegex)) {
            throw new CheckException("Text shouldn't be strange");
        }

        if (type == StringType.TITLE && string.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        } else if (type == StringType.DESCRIPTION && string.length() > 500){
            throw new CheckException("Description cannot be more than 500 characters");
        }
    }


    /**
     * Checks if the evidence date is within the project dates.
     * Also checks that the date isn't in the future
     * Throws a checkException if it's not valid.
     *
     * @param project      the project to check dates for.
     * @param evidenceDate the date of the evidence
     */
    private void checkDate(Project project, LocalDate evidenceDate) {
        if (evidenceDate.isBefore(project.getStartDateAsLocalDateTime().toLocalDate())
                || evidenceDate.isAfter(project.getEndDateAsLocalDateTime().toLocalDate())) {
            throw new CheckException("Date is outside project dates");
        }

        if (evidenceDate.isAfter(LocalDate.now())){
            throw new CheckException("Date is in the future");
        }
    }


    /**
     * Creates a new evidence object and saves it to the repository. Adds and saves any web link objects and categories
     * to the evidence object.
     *
     * @param principal   The authentication principal
     *
     * @return The evidence object, after it has been added to the database.
     * @throws MalformedURLException When one of the web links has a malformed url
     */
    public Evidence addEvidence(Authentication principal,
                                EvidenceDTO evidenceDTO) throws MalformedURLException {
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        long projectId = evidenceDTO.getProjectId();
        String title = evidenceDTO.getTitle();
        String description = evidenceDTO.getDescription();
        List<WebLinkDTO> webLinks = evidenceDTO.getWebLinks();
        String date = evidenceDTO.getDate();
        List<String> categories = evidenceDTO.getCategories();

        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            throw new CheckException("Project Id does not match any project");
        }
        Project project = optionalProject.get();
        LocalDate localDate = LocalDate.parse(date);
        checkDate(project, localDate);

        checkString(title, StringType.TITLE);
        checkString(description, StringType.DESCRIPTION);

        Evidence evidence = new Evidence(user.getId(), title, localDate, description);

        for (WebLinkDTO dto : webLinks) {
            WebLink webLink = new WebLink(evidence, dto.getName(), dto.getUrl());
            webLinkRepository.save(webLink);
            evidence.addWebLink(webLink);
        }

        this.addSkills(evidence, evidenceDTO.getSkills());

        for (String categoryString : categories) {
            switch (categoryString) {
                case "SERVICE" -> evidence.addCategory(Category.SERVICE);
                case "QUANTITATIVE" -> evidence.addCategory(Category.QUANTITATIVE);
                case "QUALITATIVE" -> evidence.addCategory(Category.QUALITATIVE);
                default -> logger.warn("Evidence service - evidence {} attempted to add category {}", evidence.getId(), categoryString);
            }
        }
        return evidenceRepository.save(evidence);
    }


    /**
     * Add a list of skills to a given piece of evidence
     *
     * @param evidence - The  piece of evidence
     * @param skills - The list of the skills in string form
     */
    public void addSkills(Evidence evidence, List<String> skills) {
        for(String skillName: skills){
            if (skillName == null || skillName.equals("") || skillName.equals(" ")){
                continue;
            }
            Optional<Skill> optionalSkill = skillRepository.findByNameIgnoreCase(skillName);
            Skill theSkill;
            if (optionalSkill.isEmpty()) {
                Skill createSkill = new Skill(skillName);
                theSkill = skillRepository.save(createSkill);
            } else {
                theSkill = optionalSkill.get();
            }
            evidence.addSkill(theSkill);
            evidenceRepository.save(evidence);
        }
    }
}
