package hudson.plugins.analysis.views;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.User;
import hudson.plugins.analysis.util.model.Author;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import hudson.tasks.Mailer;
import hudson.plugins.git.GitSCM;

/**
 * Details for a particular person.
 *
 * @author jgibson
 */
public class AuthorDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5907296989102083012L;

    /** The autor to show the details for. */
    private final String authorName;
    private final String authorEmail;

    @SuppressWarnings("Se")
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

    /**
     * Creates a new instance of {@code AuthorDetail}.
     *
     * @param owner            current build as owner of this action.
     * @param detailFactory    factory to create detail objects with
     * @param authorContainer the author to show the details for
     * @param defaultEncoding  the default encoding to be used when reading and parsing files
     * @param header           header to be shown on detail page
     */
    public AuthorDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Author authorContainer, final String defaultEncoding, final String header) {
        super(owner, detailFactory, authorContainer.getAnnotations(), defaultEncoding, header, authorContainer.getHierarchy());
        this.authorName = authorContainer.getFullName();
        this.authorEmail = authorContainer.getEmail();

    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "".equals(authorName) ? "Unknown users" : authorName;
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
        if ("".equals(authorName)) {
            return null;
        }
        Run<?, ?> run = getOwner();
        if (run.getParent() instanceof AbstractProject) {
            AbstractProject aProject = (AbstractProject) run.getParent();
            SCM scm = aProject.getScm();


            if ((scm == null) || (scm instanceof NullSCM)) {
                scm = aProject.getRootProject().getScm();
            }
            try {
                user = findOrCreateUser(authorName, authorEmail, scm);
            }
            catch (NoClassDefFoundError e) {
                // Git wasn't installed, ignore
            }
        }
        return user;
    }


    /**
     * Returns user of the change set.  Stolen from hudson.plugins.git.GitChangeSet.
     *
     * @param fullName user name.
     * @param email user email.
     * @param scm the SCM of the owning project.
     * @return {@link User} or {@code null} if the {@Code SCM} isn't a {@code GitSCM}.
     */
    public static User findOrCreateUser(final String fullName, final String email, final SCM scm) {
        if (!(scm instanceof GitSCM)) {
            return null;
        }

        GitSCM gscm = (GitSCM) scm;
        boolean createAccountBasedOnEmail = gscm.isCreateAccountBasedOnEmail();

        User user;
        if (createAccountBasedOnEmail) {
            user = User.get(email, false);

            if (user == null) {
                try {
                    user = User.get(email, true);
                    user.setFullName(fullName);
                    user.addProperty(new Mailer.UserProperty(email));
                    user.save();
                }
                catch (IOException e) {
                    // add logging statement?
                }
            }
        }
        else {
            user = User.get(fullName, false);

            if (user == null) {
                user = User.get(email.split("@")[0], true);
            }
        }
        return user;
    }
}
