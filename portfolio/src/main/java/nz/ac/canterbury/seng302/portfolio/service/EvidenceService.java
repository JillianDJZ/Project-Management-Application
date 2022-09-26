package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;


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

        if (evidenceDate.isAfter(LocalDate.now())) {
            throw new CheckException("Date is in the future");
        }
    }


    /**
     * Creates a new evidence object and saves it to the repository. Adds and saves any web link objects and categories
     * to the evidence object.
     *
     * @param principal The authentication principal
     * @return The evidence object, after it has been added to the database.
     * @throws MalformedURLException When one of the web links has a malformed url
     */
    public Evidence addEvidence(Authentication principal,
                                EvidenceDTO evidenceDTO) throws MalformedURLException, CheckException {
        logger.info("CREATING EVIDENCE - Attempting to create evidence with title: {}", evidenceDTO.getTitle());
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        checkValidEvidenceDTO(evidenceDTO);

        List<Integer> associates = evidenceDTO.getAssociateIds();
        if (associates == null) {
            associates = new ArrayList<>();
        }
        associates = new ArrayList<>(new LinkedHashSet<>(associates));
        associates.remove((Object) user.getId());
        associates.add(user.getId());

        return createEvidenceForAssociates(evidenceDTO, associates);
    }


    public Evidence editEvidence(Authentication principal,
                                 EvidenceDTO evidenceDTO) throws MalformedURLException {
        logger.info("EDITING EVIDENCE - Attempting to edit evidence with title: {}", evidenceDTO.getTitle());
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        Optional<Evidence> optionalOriginalEvidence = evidenceRepository.findById(evidenceDTO.getId());
        if (optionalOriginalEvidence.isEmpty()) {
            String message = "No evidence found with id: " + evidenceDTO.getId();
            logger.warn("Failed to edit evidence with message - {}", message);
            throw new CheckException(message);
        }
        Evidence originalEvidence = optionalOriginalEvidence.get();
        checkValidEvidenceDTO(evidenceDTO);
        List<Integer> associates = evidenceDTO.getAssociateIds();
        if (associates == null) {
            associates = new ArrayList<>();
        }
        associates = new ArrayList<>(new LinkedHashSet<>(associates));
        associates.add(user.getId());
        // Create new evidence for new associated users.
        List<Integer> unassociatedUsers = getUnassociatedUsers(originalEvidence, associates);
        if (!unassociatedUsers.isEmpty()) {
            createEvidenceForAssociates(evidenceDTO, unassociatedUsers);
        }
        return updateExistingEvidence(originalEvidence, evidenceDTO);
    }



    private void checkValidEvidenceDTO(EvidenceDTO evidenceDTO) {
        long projectId = evidenceDTO.getProjectId();
        String title = evidenceDTO.getTitle();
        String description = evidenceDTO.getDescription();
        List<WebLinkDTO> webLinks = evidenceDTO.getWebLinks();
        String date = evidenceDTO.getDate();

        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            throw new CheckException("Project Id does not match any project");
        } else if (webLinks.size() > 10) {
            throw new CheckException("This piece of evidence has too many weblinks attached to it; 10 is the limit");
        }
        Project project = optionalProject.get();
        LocalDate localDate = LocalDate.parse(date);
        checkDate(project, localDate);

        regexService.checkInput(RegexPattern.GENERAL_UNICODE, title, 5, 50, "Title");
        regexService.checkInput(RegexPattern.GENERAL_UNICODE, description, 5, 500, "Description");
    }


    public Evidence createEvidenceForAssociates(EvidenceDTO evidenceDTO, List<Integer> associates) throws MalformedURLException {
        Evidence ownerEvidence = null;
        for (Integer ownersId : associates) {
            checkAssociateId(ownersId);
            ownerEvidence = addEvidenceForUser(ownersId, evidenceDTO, associates);
            addWeblinks(ownerEvidence, evidenceDTO.getWebLinks());
            addSkills(ownerEvidence, evidenceDTO.getSkills());
        }
        return ownerEvidence;
    }


    private Evidence updateExistingEvidence(Evidence originalEvidence, EvidenceDTO evidenceDTO) throws MalformedURLException {
        originalEvidence.setTitle(evidenceDTO.getTitle());
        originalEvidence.setDescription(evidenceDTO.getDescription());
        originalEvidence.setDate(LocalDate.parse(evidenceDTO.getDate()));
        originalEvidence.clearCategories();
        addCategoriesToEvidence(originalEvidence, evidenceDTO.getCategories());
        originalEvidence.clearSkills();
        addSkills(originalEvidence, evidenceDTO.getSkills());
        originalEvidence.clearWeblinks();
        addWeblinks(originalEvidence, evidenceDTO.getWebLinks());
        originalEvidence.clearAssociatedIds();
        for (Integer userId : evidenceDTO.getAssociateIds()) {
            originalEvidence.addAssociateId(userId);
        }
        return evidenceRepository.save(originalEvidence);
    }


    /**
     * Helper method that adds a piece of evidence to the specified user id
     *
     * @param userId The id of the user that you are adding evidence to
     * @param title The title of the evidence
     * @param description The description of the evidence
     * @param localDate The date of the evidence, in localDate form
     * @param categories The categories of the evidence
     * @param associateIds The user ids of any associated users.
     *                     This should include the original creator of the evidence.
     * @return the evidence object after saving it in the evidence repository
     */
    private Evidence addEvidenceForUser(int userId, EvidenceDTO evidenceDTO, List<Integer> associateIds) {
        logger.info("CREATING EVIDENCE - attempting to create evidence for user: {}", userId);
        Evidence evidence = new Evidence(userId, evidenceDTO.getTitle(), LocalDate.parse(evidenceDTO.getDate()), evidenceDTO.getDescription());

        addCategoriesToEvidence(evidence, evidenceDTO.getCategories());
        logger.info("Adding associate IDs: {}", associateIds);
        for (Integer associate : associateIds) {
            evidence.addAssociateId(associate);
        }

        return evidenceRepository.save(evidence);
    }


    private void addCategoriesToEvidence(Evidence evidence, List<String> categories) {
        for (String categoryString : categories) {
            switch (categoryString) {
                case "SERVICE" -> evidence.addCategory(Category.SERVICE);
                case "QUANTITATIVE" -> evidence.addCategory(Category.QUANTITATIVE);
                case "QUALITATIVE" -> evidence.addCategory(Category.QUALITATIVE);
                default -> logger.warn("Evidence service - evidence {} attempted to add category {}", evidence.getId(), categoryString);
            }
        }
    }


    /**
     * Add a list of skills to a given piece of evidence. If the skills name is 'No Skills' it is ignored
     *
     * @param evidence - The  piece of evidence
     * @param skills   - The list of the skills in string form
     */
    public void addSkills(Evidence evidence, List<String> skills) {
        for(String skillName: skills){
            try {
                regexService.checkInput(RegexPattern.GENERAL_UNICODE, skillName, 1, 30, "Skill name");
            } catch (CheckException e) {
                removeWeblinks(evidence);
                evidenceRepository.delete(evidence);
                throw new CheckException(e.getMessage());
            }
            Optional<Skill> optionalSkill = skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(evidence.getUserId(), skillName);
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
        }
        evidenceRepository.save(evidence);
    }


    /**
     * Deletes all the weblinks associated with a piece of evidence. This is needed as we are unable to delete evidence
     * if it has weblinks saved to it
     *
     * @param evidence The evidence with weblinks to delete
     */
    private void removeWeblinks(Evidence evidence) {
        webLinkRepository.deleteAll(evidence.getWebLinks());
    }


    /**
     * Helper method to add a list of weblinks to a piece of evidence
     *
     * @param evidence The evidence to add the weblinks to
     * @param webLinks The list of weblinks to add, in their raw DTO form
     * @throws MalformedURLException if a weblink has an invalid URL
     */
    private void addWeblinks(Evidence evidence, List<WebLinkDTO> webLinks) throws MalformedURLException {
        for (WebLinkDTO dto : webLinks) {
            URL weblinkURL = new URL(dto.getUrl());
            if (dto.getUrl().contains("&nbsp")) {
                evidenceRepository.delete(evidence);
                throw new MalformedURLException("The non-breaking space is not a valid character");
            }
            try {
                weblinkURL.toURI(); // The toURI covers cases that the URL constructor does not, so we use both
            } catch (URISyntaxException e) {
                evidenceRepository.delete(evidence);
                throw new CheckException("The URL for the weblink " + dto.getName() + " is not correctly formatted.");
            }
            // This requires the evidence object to be saved, since it needs to refer to it
            WebLink webLink = new WebLink(evidence, dto.getName(), weblinkURL);
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, dto.getName(), 1, 50, "web link name");
            webLinkRepository.save(webLink);
            evidence.addWebLink(webLink);
            evidenceRepository.save(evidence);
        }
    }


    /**
     * Takes a piece of evidence and updates the associated users for that evidence.
     *
     * If the piece of evidence has new associated users then the evidence is created
     * for that user too.
     */
    public List<Integer> getUnassociatedUsers(Evidence originalEvidence, List<Integer> associatedUsers) {
        List<Integer> usersToGiveEvidence = new ArrayList<>();
        for (Integer userId : associatedUsers) {
            if (! originalEvidence.getArchivedIds().contains(userId)) {
                usersToGiveEvidence.add(userId);
            }
        }
        return usersToGiveEvidence;
    }


    /**
     * Helper method that checks if a user exists.
     * Tries to find the user with the specific ID.
     * If it can't find it, throw an exception.
     *
     * @param associateId the ID of the associate/user you want to find
     */
    private void checkAssociateId(int associateId) {
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(associateId).build();
        UserResponse associate = userAccountsClientService.getUserAccountById(request);
        if (associate.getId() < 1) {
            logger.error("CREATING EVIDENCE: Bad id: {}", associateId);
            throw new CheckException("Could not find associated user with ID: " + associateId);
        }
    }
}
