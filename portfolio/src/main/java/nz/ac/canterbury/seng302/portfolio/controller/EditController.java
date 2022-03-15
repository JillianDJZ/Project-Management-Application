package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for the edit page
 */
@Controller
public class EditController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /**
     * The main/entry endpoint into the edit page
     * I'm not entirely sure why this method in particular needs to have the form-objects created
     * But the page won't run without it
     * My best guess is it's something to do with the endpoints being different
     *
     * @param principal The authentication state
     * @param editInfo A thymeleaf-created object
     * @param passInfo A thymeleaf-created object
     * @param model The thymeleaf model
     * @return The thymeleaf template
     */
    @RequestMapping("/edit")
    public String edit(
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editDetailsForm") UserRequest editInfo,
            @ModelAttribute(name="editPasswordForm") PasswordRequest passInfo,
            Model model
    ) {
        /*We want to fill in the form details with what the user already has
        so let's grab all those details and put them in the model
         */
        addModelAttributes(principal, model);
        return "edit";
    }

    /**
     * Helper function to add attributes to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param principal The authentication state
     * @param model The model you're adding attributes to
     */
    private void addModelAttributes(
            AuthState principal,
            Model model) {
        /*
        These addAttribute methods inject variables that we can use in our html file
        Their values have been hard-coded for now, but they can be the result of functions!
        ideally, these would be functions like getUsername and so forth
         */
        int id = Integer.parseInt(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND"));
        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(id);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(request.build());
        model.addAttribute("username", "Username:" + userResponse.getUsername());
        model.addAttribute("email", userResponse.getEmail());
        model.addAttribute("firstname", userResponse.getFirstName());
        model.addAttribute("middlename", userResponse.getMiddleName());
        model.addAttribute("lastname", userResponse.getLastName());
        model.addAttribute("nickname", userResponse.getNickname());
        model.addAttribute("pronouns", userResponse.getPersonalPronouns());
        model.addAttribute("userBio", userResponse.getBio());
    }




}
