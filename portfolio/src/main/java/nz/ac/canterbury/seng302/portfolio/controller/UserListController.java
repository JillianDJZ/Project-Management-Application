package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import java.util.List;
import java.util.ArrayList;

@Controller
public class UserListController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private int pageNum = 1;
    private final int usersPerPageLimit = 50;
    private int offset = 0;
    private int numUsers= 0;
    private int totalPages = 1;
    private String sortOrder = "name-increasing";
    private final ArrayList<Integer> footerNumberSequence = new ArrayList<>();
    private List<UserResponse> userResponseList;
    HashMap<String, UserRole> stringToRole;

    /**
     * Used to create the list of users, 50 per page, by default sorted by users names. Adds all these values on
     * the webpage to be displayed. Also used for the other pages in the user list. Passes through users as well as
     * information needed to create the navigation
     *
     * @param model model Parameters sent to thymeleaf template to be rendered into HTML
     * @param page an optional integer parameter that is used to get the correct page of users
     * @return Message generated by IdP about authenticate attempt
     */
    @GetMapping("/user-list")
    public ModelAndView getUserList(
            @AuthenticationPrincipal AuthState principal,
            Model model,
          @RequestParam(name = "page", required = false) Integer page,
          @RequestParam(name = "sortField", required = false) String order)
    {
        if (order != null) {
            sortOrder = order;
        }
        if (page != null) {
            pageNum = page;
        }

        if (pageNum <= 1) { //to ensure no negative page numbers
            pageNum = 1;
        }
        offset = (pageNum - 1) * usersPerPageLimit;

        PaginatedUsersResponse response = getPaginatedUsersFromServer();
        numUsers = response.getResultSetSize();
        totalPages = numUsers / usersPerPageLimit;
        if ((numUsers % usersPerPageLimit) != 0) {
            totalPages++;
        }
        if(pageNum > totalPages) { //to ensure that the last page will be shown if the page number is too large
            pageNum = totalPages;
            offset = (pageNum - 1) * usersPerPageLimit;
            response = getPaginatedUsersFromServer();
        }
        
        createFooterNumberSequence();
        userResponseList = response.getUsersList();
        addAttributesToModel(principal, model);

        return new ModelAndView("user-list");
    }

    /**
     * Adds to the model the attributes required to display, format, and interact with the user list table.
     *
     * @param model the model to which the attributes will be added.
     */
    private void addAttributesToModel(AuthState principal, Model model) {
        UserRole[] possibleRoles = UserRole.values();
        possibleRoles = Arrays.stream(possibleRoles).filter(role -> role != UserRole.UNRECOGNIZED).toArray(UserRole[]::new);
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has. Adds boolean object to the view so that displays can be changed on the frontend.
        List<UserRole> roles = userResponse.getRolesList();
        if (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            model.addAttribute("userCanEdit", true);
        } else {
            model.addAttribute("userCanEdit", false);
        }

        model.addAttribute("user", userResponse);

        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalItems", numUsers);
        model.addAttribute("user_list", userResponseList);
        model.addAttribute("footerNumberSequence", footerNumberSequence);
        model.addAttribute("possibleRoles", possibleRoles);
        model.addAttribute("sortOrder", sortOrder);
    }


    /**
     * Deletes a selected user role from a requested user, using a ModifyRoleOfUserRequest to communicate the user ID
     * and role of the user to be changed. Only authenticated users with teacher/course administrator permissions can
     * perform role deletions.
     *
     * @param userId The user ID of the user being edited.
     * @param roleString The role being deleted from the user, in a string format.
     * @return The success status of the deletion.
     */

    @DeleteMapping("/editUserRole")
    public ResponseEntity<String> deleteUserRole(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "role") String roleString) {
        int adminstrator = PrincipalAttributes.getIdFromPrincipal(principal); //TODO use this for authenticating (teacher or admin)
        logger.info("Deleting user role " + roleString + " from user " + userId);
        ModifyRoleOfUserRequest request = formUserRoleChangeRequest(userId, roleString);
        UserRoleChangeResponse response = userAccountsClientService.removeRoleFromUser(request);

        if (response.getIsSuccess())
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>("A user cannot have no roles", HttpStatus.FORBIDDEN);
    }


    /**
     * Adds a selected user role to a requested user, using a ModifyRoleOfUserRequest to communicate the user ID
     * and role of the user to be changed. Only authenticated users with teacher/course administrator permissions can
     * perform role additions.
     *
     * @param principal Allows the user ID of the user making the request to be retrieved.
     * @param userId The user ID of the user being edited.
     * @param roleString The role being added to the user, in a string format.
     * @return The success status of the addition.
     */

    @PutMapping("/editUserRole")
    public ResponseEntity<String> addUserRole(
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(value = "userId") String userId,
            @RequestParam(value = "role") String roleString) {
        int adminstrator = PrincipalAttributes.getIdFromPrincipal(principal); //TODO use this for authenticating (teacher or admin)
        // ToDo if the user is not authenticated as a teacher or admin they can't change the role so return 401
        logger.info("Adding user role " + roleString + " to user " + userId);
        ModifyRoleOfUserRequest request = formUserRoleChangeRequest(userId, roleString);
        UserRoleChangeResponse response = userAccountsClientService.addRoleToUser(request);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Helper method for adding and deleting user roles end points. This method extracts the common request formation
     * as both adding and deleting requests use the same message format.
     * <br>
     * @param userId - The userId of the user whose roles are being edited
     * @param roleString - A string representation of the role of the user to be added. converted with a dict to a UserRole
     * @return request - the ModifyRoleOfUserRequest that will be sent over grpc
     */
    private ModifyRoleOfUserRequest formUserRoleChangeRequest(String userId, String roleString) {
        if (stringToRole == null) {
            populateRolesDict();
        }
        return ModifyRoleOfUserRequest.newBuilder()
                .setRole(stringToRole.get(roleString))
                .setUserId(Integer.parseInt(userId))
                .build();
    }


    /**
     * A helper function to get the values of the offset and users per page limit and send a request to the client
     * service, which then gets a response from the server service
     *
     * @return PaginatedUsersResponse, a type that contains all users for a specific page and the total number of users
     */
    private PaginatedUsersResponse getPaginatedUsersFromServer(){
        GetPaginatedUsersRequest.Builder request = GetPaginatedUsersRequest.newBuilder();
        request.setOffset(offset);
        request.setLimit(usersPerPageLimit);
        request.setOrderBy(sortOrder);
        return userAccountsClientService.getPaginatedUsers(request.build());
    }


    /**
     * This is used to set the numbers at the bottom of the screen for page navigation. Otherwise, at larger page values
     * it gets very messy. Creates a range of -5 to +5 from the current page if able to
     */
    private void createFooterNumberSequence(){
        footerNumberSequence.clear();

        int minNumber = 1;
        int maxNumber = 11;

        if (totalPages < 11) {
            maxNumber = totalPages;
        } else if (pageNum > 6) {
            if (pageNum + 5 < totalPages) {
                minNumber = pageNum - 5;
                maxNumber = pageNum + 5;
            } else {
                maxNumber = totalPages;
                minNumber = totalPages - 10;
            }
        }

        for (int i = minNumber; i <= maxNumber; i++) {
            footerNumberSequence.add(i);
        }
    }

    /**
     * Used to set a userAccountClientService if not using the autowired one. Useful for testing and mocking
     * @param service The userAccountClientService to be used
     */
    public void setUserAccountsClientService(UserAccountsClientService service) { this.userAccountsClientService = service;}

    /**
     * To get the list of users for the specific page number
     * @return a list of users
     */
    public List<UserResponse> getUserResponseList() { return this.userResponseList; }

    /**
     * to get the list of page numbers that is displayed at the bottom of the page for navigation
     * @return an ArrayList of numbers used for the navigation
     */
    public ArrayList<Integer> getFooterSequence() { return this.footerNumberSequence; }

    /**
     * To get the string describing how to sort the data
     * @return a string of how to data is to be sorted
     */
    public String getSortOrder() { return this.sortOrder;}

    private void populateRolesDict() {
        stringToRole = new HashMap<>();
        stringToRole.put("STUDENT", UserRole.STUDENT);
        stringToRole.put("TEACHER", UserRole.TEACHER);
        stringToRole.put("COURSE_ADMINISTRATOR", UserRole.COURSE_ADMINISTRATOR);
        stringToRole.put("UNRECOGNIZED", UserRole.UNRECOGNIZED);
    }

}
