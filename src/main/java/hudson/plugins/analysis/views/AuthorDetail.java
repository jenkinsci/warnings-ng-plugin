package hudson.plugins.analysis.views;

import java.util.Collections;

import hudson.model.Run;
import hudson.model.User;
import hudson.plugins.analysis.util.model.Author;

/**
 * Details for a particular person.
 *
 * @author John Gibson
 */
public class AuthorDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5907296989102083012L;

    private final String authorName;
    private final String authorEmail;

    private transient boolean userAttempted;
    private transient User user;

    /**
     * Creates a new instance of {@code AuthorDetail}.
     *
     * @param owner            current build as owner of this action.
     * @param detailFactory    factory to create detail objects with
     * @param authorContainer the author to show the details for
     * @param defaultEncoding  the default encoding to be used when reading and parsing files
     * @param header           header to be shown on detail page
     */
    public AuthorDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final Author authorContainer, final String defaultEncoding, final String header) {
        super(owner, detailFactory, authorContainer.getAnnotations(), defaultEncoding, header, authorContainer.getHierarchy());

        this.authorName = authorContainer.getFullName();
        this.authorEmail = authorContainer.getEmail();
    }

    @Override
    public String getDisplayName() {
        return authorName;
    }

    /**
     * Get a {@code User} that corresponds to this author.
     *
     * @return a {@code User} or {@code null} if one can't be created.
     */
    public User getUser() {
        if (userAttempted) {
            return user;
        }
        userAttempted = true;
        user = User.get(authorEmail,false, Collections.emptyMap());
        if (user == null) {
            user = User.get(authorName,false, Collections.emptyMap());
            if (user == null) {
                user = User.getUnknown();
            }
        }
        return user;
    }
}
