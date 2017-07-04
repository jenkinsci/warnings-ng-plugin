package hudson.plugins.analysis.util.model;

/**
 * A container for author annotations.
 *
 * @author John Gibson
 */
public class Author extends AnnotationContainer {
    private static final long serialVersionUID = 5504146567211894175L;

    private String displayName;
    private final String fullName;
    private final String email;

    /**
     * Creates a new instance of {@link Author}.
     *
     * @param key         key of this container
     * @param displayName the display name of this container
     * @param authorName  the full name of the author for this container
     * @param authorEmail the email of the author for this container
     * @param hierarchy   the scope of this author container. Should be one of the {@code USER_} values.
     */
    public Author(final String key, final String displayName, final String authorName, final String authorEmail,
            final Hierarchy hierarchy) {
        super(key, hierarchy);
        this.displayName = displayName;

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
        return displayName;
    }

    /**
     * Returns a dummy object for unknown authors.
     *
     * @return unknown author dummy
     */
    public static Author unknown() {
        return new Author(AbstractAnnotation.DEFAULT_CATEGORY, AbstractAnnotation.DEFAULT_CATEGORY, AbstractAnnotation.DEFAULT_CATEGORY,
                AbstractAnnotation.DEFAULT_CATEGORY, Hierarchy.USER);
    }
}
