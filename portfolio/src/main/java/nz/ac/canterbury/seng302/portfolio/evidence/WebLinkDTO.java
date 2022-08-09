package nz.ac.canterbury.seng302.portfolio.evidence;

/**
 * Weblink Data Transfer Object, which represents a valid web link with an alias.
 */
public class WebLinkDTO {

    /** The readable name of the link */
    private final String name;

    /** The followable URL */
    private final String url;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public WebLinkDTO(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
