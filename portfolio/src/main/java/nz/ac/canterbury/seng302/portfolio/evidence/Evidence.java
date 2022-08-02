package nz.ac.canterbury.seng302.portfolio.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nz.ac.canterbury.seng302.portfolio.CheckException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an Evidence entity
 */
@Entity
public class Evidence {

    @Id
    @GeneratedValue
    private int id;

    private int userId;
    private String title;
    private LocalDateTime date;
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "evidence")
    private List<WebLink> webLinks;


    /**
     * Constructs an instance of the evidence object
     *
     * @param userId the user associated with the evidence
     * @param title the title of the evidence
     * @param date the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int userId, String title, LocalDateTime date, String description) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        }
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        }
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.description = description;
    }


    /**
     * This constructor is used for testing only! Constructs an instance of the evidence object
     *
     * @param evidenceId the ID of the evidence with is typically Generated automatically.
     * @param userId the user associated with the evidence
     * @param title the title of the evidence
     * @param date the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int evidenceId, int userId, String title, LocalDateTime date, String description) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        }
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        }
        this.id = evidenceId;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.description = description;
    }


    /**
     * Default JPA Evidence constructor
     */
    public Evidence() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        } else {
            this.title = title;
        }

    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        } else {
            return description;
        }

    }

    public List<WebLink> getWebLinks() {
        return webLinks;
    }

    public void addWebLink(WebLink webLink){
        this.webLinks.add(webLink);
    }

    public void addWebLinks(List<WebLink> webLinks){
        this.webLinks.addAll(webLinks);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * This method is used to help with testing. It returns the expected JSON string created for this object.
     *
     * Note that the date string must be shortened by 2 characters as this is done implicitly when the object is
     * Jsonified.
     *
     * @return the Json string the represents this piece of evidence.
     */
    public String toJsonString() {
        int trailingZeros = 0;
        while (date.toString().charAt(date.toString().length() - (1 + trailingZeros)) == '0') {
            trailingZeros++;
        }
        String correctedDate = date.toString().substring(0, date.toString().length() - trailingZeros); // see docstring
        return  "{" +
                "\"id\":" + id +
                ",\"userId\":" + userId +
                ",\"title\":\"" + title +
                "\",\"date\":\"" + correctedDate +
                "\",\"description\":\"" + description +
                "\"}";
    }
}
