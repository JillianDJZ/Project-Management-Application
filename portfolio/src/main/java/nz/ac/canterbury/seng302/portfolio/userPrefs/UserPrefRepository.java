package nz.ac.canterbury.seng302.portfolio.userPrefs;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface that defines how to interact with the database. Spring boot does the hard work under the hood
 * to actually implement these functions.
 * @see  <a href="https://spring.io/guides/gs/accessing-data-jpa">https://spring.io/guides/gs/accessing-data-jpa/</a>
 */
public interface UserPrefRepository extends CrudRepository<UserPrefs, Integer> {

    /**
     * Gets a user's preference 'object' from the database using the id of said user
     * @return The user's preferences, in the form of a UserPrefs object
     */
    UserPrefs getUserPrefsByUserId(int userId);
}
