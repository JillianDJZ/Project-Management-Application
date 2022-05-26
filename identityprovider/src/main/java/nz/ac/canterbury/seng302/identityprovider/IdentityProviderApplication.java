package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The main IdP application class using springboot.
 */
@SpringBootApplication
public class IdentityProviderApplication {

    /** Enables us to directly inject test users into the database*/
    @Autowired
    UserRepository repository;

    /** Enables us to directly inject our two main groups (Teachers and Non-member group) into the database */
    @Autowired
    GroupRepository groupRepository;

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Turn on (true) to create the default admin account */
    private boolean includeAdminAccount = true;

    /** Turn on (true) to create the 1000 test accounts */
    private boolean includeTestData = true;

    /**
     * Initialises test data when the boolean variables are true
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        if (includeAdminAccount)
            addAdminAccount();
        if (includeTestData)
            addTestUsers();

//        // Create the two main groups we need, teachers and non group group.
//        Group teachingGroup = new Group(0,"Teachers", "Teaching Staff");
//        Group nonGroupGroup = new Group(1,"Non-Group", "Members Without A Group");
//
//        List<User> everyUserList = (List<User>) repository.findAll();
//        List<User> teachers = everyUserList.stream().filter(p -> p.getRoles().contains(UserRole.TEACHER)).toList();
//        List<User> nonGroupUsers = everyUserList.stream().filter(p -> !p.getRoles().contains(UserRole.TEACHER)).toList();
//
//        teachingGroup.addAllUsersToGroup(teachers.stream().map(User::getId).toList());
//        nonGroupGroup.addAllUsersToGroup(nonGroupUsers.stream().map(User::getId).toList());
//
//
//        groupRepository.save(teachingGroup);
//        groupRepository.save(nonGroupGroup);
    }


    /**
     * Main method see class documentation.
     * @param args - default main params
     */
    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }

    // ----------------------------------------- Test data ---------------------------------------------------

    /**
     * Adds the default admin user
     */
    private void addAdminAccount() {
        logger.info("Initialising Admin user");
        User admin = new User(
                "admin",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        admin.addRole(UserRole.COURSE_ADMINISTRATOR);
        repository.save(admin);
    }


    /**
     * Adds the 1000 default test users
     */
    private void addTestUsers() {
        logger.info("Initialising test user Steve");
        User steve = new User(
                "steve",
                "password",
                "Steve",
                "McSteve",
                "Steveson",
                "Stev",
                "kdsflkdjf",
                "Steve/Steve",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        steve.addRole(UserRole.TEACHER);
        repository.save(steve);

        logger.info("Initialising test user Student");
        User student = new User(
                "student",
                "password",
                "Steve",
                "McSteveon",
                "Steveson",
                "Stev",
                "kdsflkdjf",
                "Steve/Steve",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        student.addRole(UserRole.STUDENT);
        repository.save(student);

        for (int i = 0; i < 1000; i++) {
            User lemming = new User(
                    "User " + i,
                    "password",
                    "Steve",
                    "McSteve",
                    "Steveson",
                    "Stev",
                    "kdsflkdjf",
                    "Steve/Steve",
                    "steve@example.com",
                    TimeService.getTimeStamp()
            );
            repository.save(lemming);
        }
    }
}
