package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Category;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLinkRepository;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A utility class for more complex actions involving Evidence
 */
@Service
public class EvidenceService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserAccountsClientService userAccountsClientService;

    private final ProjectRepository projectRepository;

    private final EvidenceRepository evidenceRepository;

    private final WebLinkRepository webLinkRepository;

    private final SkillRepository skillRepository;

    private final RegexService regexService;

    @Autowired
    public EvidenceService(
            UserAccountsClientService userAccountsClientService,
            ProjectRepository projectRepository,
            EvidenceRepository evidenceRepository,
            WebLinkRepository webLinkRepository,
            SkillRepository skillRepository,
            RegexService regexService
    ) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
        this.webLinkRepository = webLinkRepository;
        this.skillRepository = skillRepository;
        this.regexService = regexService;
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

        regexService.checkInput(RegexPattern.GENERAL_UNICODE, title, 2, 50, "title");
        regexService.checkInput(RegexPattern.GENERAL_UNICODE, description, 2, 500, "description");

        Evidence evidence = new Evidence(user.getId(), title, localDate, description);
        evidence = evidenceRepository.save(evidence);

        for (WebLinkDTO dto : webLinks) {
            WebLink webLink = new WebLink(evidence, dto.getName(), dto.getUrl());
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, dto.getName(), 1, 50, "web link name");
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
     * Add a list of skills to a given piece of evidence. If the skills name is 'No Skills' it is ignored
     *
     * @param evidence - The  piece of evidence
     * @param skills - The list of the skills in string form
     */
    public void addSkills(Evidence evidence, List<String> skills) {
        for(String skillName: skills){
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, skillName, 2, 30, "skill name");
            Optional<Skill> optionalSkill = skillRepository.findByNameIgnoreCase(skillName);
            Skill theSkill;
            if (optionalSkill.isEmpty()) {
                if (skillName.equalsIgnoreCase("No Skill")) {
                    continue;
                }
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
