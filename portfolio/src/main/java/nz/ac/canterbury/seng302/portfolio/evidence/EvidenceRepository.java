package nz.ac.canterbury.seng302.portfolio.evidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

/**
 * Repository class for handling all the queries related to Evidence objects.
 */
public interface EvidenceRepository extends CrudRepository<Evidence, Integer> {

    /** Finds an Evidence object by its id. */
    @Query
    Evidence findById(int id);

    /** Returns an arrayList of all the evidence for a user in order by date descending */
    @Query
    ArrayList<Evidence> findAllByUserIdOrderByDateDesc(int id);
}