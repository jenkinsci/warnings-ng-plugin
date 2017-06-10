package hudson.plugins.analysis.util.model;

import org.apache.commons.lang3.StringUtils;

import hudson.plugins.analysis.Messages;

/**
 * A container for author annotations.
 *
 * @author John Gibson
 */
public class Author extends AnnotationContainer {
    private static final long serialVersionUID = 5504146567211894175L;

    private final String fullName;
    private final String email;

    /**
     * Creates a new instance of {@link Author}.
     *
     * @param authorName  the full name of the author for this container.
     * @param authorEmail the email of the author for this container.
     * @param hierarchy   the scope of this author container.  Should be one of the {@code USER_} values.
     */
    public Author(final String authorName, final String authorEmail, final Hierarchy hierarchy) {
        super(authorName + authorEmail, hierarchy);

        this.fullName = authorName;
        this.email = authorEmail;
    }

    /**
     * Get the full name of the author.
     *
     * @return the full name of the author.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Get the email of the author.
     *
     * @return the email of author.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get a readable name for this container.
     *
     * @return a readable name for this container.
     */
    public String getDisplayName() {
        return StringUtils.defaultString(getFullName(), Messages.Author_NoResult());
    }
}
