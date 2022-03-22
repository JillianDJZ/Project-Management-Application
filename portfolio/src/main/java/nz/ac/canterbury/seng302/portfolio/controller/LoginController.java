package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.authentication.AuthenticationException;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

    @Autowired
    public AuthenticateClientService authenticateClientService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Shows the login page to anyone who wants to see it.
     * @return The Thymeleaf login html template.
     */
    @GetMapping("/login")
    public String showLogin() {
        logger.info("GET REQUEST /login - get log in page");
        return "login";
    }

    /**
     * Attempts to authenticate with the Identity Provider via gRPC.
     *
     * This process works in a few stages:
     *  1.  We send the username and password to the IdP
     *  2.  We check the response, and if it is successful we add a cookie to the HTTP response so that
     *      the client's browser will store it to be used for future authentication with this service.
     *  3.  We return the thymeleaf login template with the 'message' given by the identity provider,
     *      this message will be something along the lines of "Logged in successfully!",
     *      "Bad username or password", etc.
     *
     * @param request HTTP request sent to this endpoint
     * @param response HTTP response that will be returned by this endpoint
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Message generated by IdP about authenticate attempt
     */
    @PostMapping("/login")
    public ModelAndView login(
            HttpServletRequest request,
            HttpServletResponse response,
            @ModelAttribute(name="loginForm") UserRequest userRequest,
            ModelMap model
    ) {
        logger.info("POST REQUEST /login - attempt to log in user");
        AuthenticateResponse loginReply;
        //This try/catch block is the login attempt
        try {
            loginReply = attemptLogin(userRequest, request, response, authenticateClientService);
        } catch (AuthenticationException e){
            // Note this is logged when the error is thrown
            model.addAttribute("errorMessage", "Error connecting to Identity Provider... " +
                    "Try again later.");
            return new ModelAndView("login");
        }
        // If login was successful redirect to account, otherwise add failure message
        if (loginReply.getSuccess()) {
            logger.info("Login Successful - redirect to account page for username " + loginReply.getUsername());
            return new ModelAndView("redirect:/account");
        } else {
            // Logged in attempt login method
            model.addAttribute("errorMessage", loginReply.getMessage());
            return new ModelAndView("login");
        }
    }

    /**
     * This method attempts to authenticate a user by sending an Authentication request to the server and if successful
     * adding a Cookie, otherwise it does not add the cookie
     *
     * @param userRequest - The userRequest object with the authentication fields
     * @param request - used for creating the cookie
     * @param response - used for creating the cookie
     * @return authenticate response - contains information about the authentication attempt.
     * @throws AuthenticationException - if the Identity provider can't be reached.
     */
    public AuthenticateResponse attemptLogin(UserRequest userRequest,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     AuthenticateClientService authenticateClientService) throws AuthenticationException {
        AuthenticateResponse authenticateResponse;
        //This try/catch block is the login attempt
        try {
            authenticateResponse = authenticateClientService.authenticate(userRequest.getUsername(), userRequest.getPassword());
        } catch (StatusRuntimeException e){
            logger.error("Error connecting to Identity Provider");
            throw new AuthenticationException("failed to connect to the Identity Provider");
        }
        //If the login was successful, create a cookie!
        if (authenticateResponse.getSuccess()) {
            logger.info("Login successful - Added cookie to username " + authenticateResponse.getUsername());
            var domain = request.getHeader("host");
            CookieUtil.create(
                    response,
                    "lens-session-token",
                    authenticateResponse.getToken(),
                    true,
                    5 * 60 * 60, // Expires in 5 hours
                    domain.startsWith("localhost") ? null : domain
            );
        } else {
            logger.info(authenticateResponse.getMessage());
        }
        return authenticateResponse;
    }
}
