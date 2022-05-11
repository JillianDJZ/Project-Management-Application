package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.RemoveNotificationInterceptor;
import nz.ac.canterbury.seng302.portfolio.service.RoleBasedIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Config implements WebMvcConfigurer
{

    /**
     * This will intercept all the endpoints that we specify in the method and run them through RoleBasedInterceptor
     * first. The RoleBasedInterceptor only allows users to continue if they are a teacher or admin
     * @param registry Registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        List<String> pathsToInterceptForRoleBased = new ArrayList<>();
        List<String> pathsToInterceptForNotificationRemoval = new ArrayList<>();

        // User Roles
        pathsToInterceptForRoleBased.add("/editUserRole");

        // Events
        pathsToInterceptForRoleBased.add("/addEvent");
        pathsToInterceptForRoleBased.add("/deleteEvent");
        pathsToInterceptForRoleBased.add("/editEvent");

        // Portfolio
        pathsToInterceptForRoleBased.add("/editProject");
        pathsToInterceptForRoleBased.add("/projectEdit");
        pathsToInterceptForRoleBased.add("/portfolio/addSprint");
        pathsToInterceptForRoleBased.add("/sprintEdit");
        pathsToInterceptForRoleBased.add("/sprintSubmit");
        pathsToInterceptForRoleBased.add("/deleteSprint");

        //Milestone
        pathsToInterceptForRoleBased.add("/editMilestone");
        pathsToInterceptForRoleBased.add("/deleteMilestone");
        pathsToInterceptForRoleBased.add("/addMilestone");


        //Deadlines
        pathsToInterceptForRoleBased.add("/addDeadline");
        pathsToInterceptForRoleBased.add("/editDeadline");
        pathsToInterceptForRoleBased.add("/deleteDeadline");






        pathsToInterceptForNotificationRemoval.add("/account");
        pathsToInterceptForNotificationRemoval.add("/portfolio");
        pathsToInterceptForNotificationRemoval.add("/calendar");
        pathsToInterceptForNotificationRemoval.add("/user-list");
        pathsToInterceptForNotificationRemoval.add("/editProject");
        pathsToInterceptForNotificationRemoval.add("/sprintEdit");
        pathsToInterceptForNotificationRemoval.add("/logout");








        registry.addInterceptor(new RoleBasedIntercepter()).addPathPatterns(pathsToInterceptForRoleBased);
        registry.addInterceptor(new RemoveNotificationInterceptor()).addPathPatterns(pathsToInterceptForNotificationRemoval);
    }
}