package hudson.plugins.analysis.views;

import java.util.Collection;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.NullSCM;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Result object representing a dynamic tab.
 *
 * @author Ulli Hafner
 */
public class TabDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    /** URL of the content to load. */
    private final String url;

    /** Whether or not we've tried to generate the commit URLs. */
    @SuppressWarnings("Se")
    private transient boolean commitUrlsAttempted;
    /** A cache of URLs for commit ids. */
    @SuppressWarnings("Se")
    private transient Map<String, URL> commitUrls;

    /**
     * Creates a new instance of {@link TabDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param annotations
     *            the module to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public TabDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        super(owner, detailFactory, annotations, defaultEncoding, "No Header", Hierarchy.PROJECT);
        this.url = url;
    }

    @Override
    public String getDisplayName() {
        return "NOT USED";
    }

    /**
     * Returns the URL that renders the content of this tab.
     *
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the jelly script the will render the details.
     *
     * @return the name of the jelly script
     */
    public String getDetails() {
        return "details.jelly";
    }

    /**
     * Returns the jelly script the will render the warnings table.
     *
     * @return the name of the jelly script
     */
    public String getWarnings() {
        return "warnings.jelly";
    }

    /**
     * Returns the jelly script the will render the fixed warnings table.
     *
     * @return the name of the jelly script
     */
    public String getFixed() {
        return "fixed.jelly";
    }

    /**
     * Creates a new instance of {@link TabDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            factory to create detail objects with
     * @param annotations
     *            the module to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @deprecated use {@link #TabDetail(Run, DetailFactory, Collection, String, String)} instead
     */
    @Deprecated
    public TabDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        this((Run<?, ?>) owner, detailFactory, annotations, url, defaultEncoding);
    }

    /**
     * Get a repository browser link for the specified commit.
     *
     * @param commitId the id of the commit to be linked.
     * @return The link or {@code null} if one is not available.
     */

    public URL urlForCommitId(final String commitId) {
        if (commitUrlsAttempted) {
            return commitUrls == null ? null : commitUrls.get(commitId);
        }
        commitUrlsAttempted = true;

        Run<?, ?> run = getOwner();
        if (run.getParent() instanceof AbstractProject) {
            AbstractProject aProject = (AbstractProject) run.getParent();
            SCM scm = aProject.getScm();
            //SCM scm = getOwner().getParent().getScm();
            if ((scm == null) || (scm instanceof NullSCM)) {
                scm = aProject.getRootProject().getScm();
            }

            final HashSet<String> commitIds = new HashSet<String>(getAnnotations().size());
            for (final FileAnnotation annot : getAnnotations()) {
                commitIds.add(annot.getAuthorCommitId());
            }
            commitIds.remove(null);
            try {
                commitUrls = computeUrlsForCommitIds(scm, commitIds);
                if (commitUrls != null) {
                    return commitUrls.get(commitId);
                }
            }
            catch (NoClassDefFoundError e) {
                // Git wasn't installed, ignore
            }
        }
        return null;
    }

    /**
     * Creates links for the specified commitIds using the repository browser.
     *
     * @param scm the {@code SCM} of the owning project.
     * @param commitIds the commit ids in question.
     * @return a mapping of the links or {@code null} if the {@code SCM} isn't a
     *  {@code GitSCM} or if a repository browser isn't set or if it isn't a
     *  {@code GitRepositoryBrowser}.
     */

    @SuppressWarnings("REC_CATCH_EXCEPTION")
    public static Map<String, URL> computeUrlsForCommitIds(final SCM scm, final Set<String> commitIds) {
        if (!(scm instanceof GitSCM)) {
            return null;
        }
        if (commitIds.isEmpty()) {
            return null;
        }

        GitSCM gscm = (GitSCM) scm;
        GitRepositoryBrowser browser = gscm.getBrowser();
        if (browser == null) {
            RepositoryBrowser<?> ebrowser = gscm.getEffectiveBrowser();
            if (ebrowser instanceof GitRepositoryBrowser) {
                browser = (GitRepositoryBrowser) ebrowser;
            }
            else {
                return null;
            }
        }

        // This is a dirty hack because the only way to create changesets is to do it by parsing git log messages
        // Because what we're doing is fairly dangerous (creating minimal commit messages) just give up if there is an error
        try {
            HashMap<String, URL> result = new HashMap<String, URL>((int) (commitIds.size() * 1.5f));
            for (final String commitId : commitIds) {
                GitChangeSet cs = new GitChangeSet(Collections.singletonList("commit " + commitId), true);
                if (cs.getId() != null) {
                    result.put(commitId, browser.getChangeSetLink(cs));
                }
            }

            return result;
        }
        // CHECKSTYLE:OFF
        catch (Exception e) {
            // CHECKSTYLE:ON
            // TODO: log?
            return null;
        }
    }
}

