package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.util.VisibleForTesting;

import j2html.tags.DomContent;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;

import static j2html.TagCreator.*;

/**
 * Renders the name of an affected file of an issue. If the affected file is accessible, then a hyper link is created.
 * Otherwise, the plain text of the base name is created.
 *
 * @author Ullrich Hafner
 */
public class FileNameRenderer {
    private final BuildFolderFacade facade;
    private final Run<?, ?> build;

    /**
     * Creates a new {@link FileNameRenderer}.
     *
     * @param build
     *         the build to obtain the affected file from
     */
    public FileNameRenderer(final Run<?, ?> build) {
        this(build, new BuildFolderFacade());
    }

    /**
     * Creates a new {@link FileNameRenderer}.
     *
     * @param build
     *         the build to obtain the affected file from
     * @param facade
     *         facade to the build folder that contains the affected files
     */
    @VisibleForTesting
    public FileNameRenderer(final Run<?, ?> build, final BuildFolderFacade facade) {
        this.facade = facade;
        this.build = build;
    }

    /**
     * Returns the String representation of an HTML link that references the UI representation of the affected file of
     * the specified issue.
     *
     * @param issue
     *         the issue to create the link for
     *
     * @return the link (if the file is accessible)
     * @see #createAffectedFileLink(Issue)
     */
    public String renderAffectedFileLink(final Issue issue) {
        return createAffectedFileLink(issue).render();
    }

    /**
     * Returns an HTML link that references the UI representation of the affected file of the specified issue.
     *
     * @param issue
     *         the issue to create the link for
     *
     * @return the link (if the file is accessible)
     */
    public DomContent createAffectedFileLink(final Issue issue) {
        return createAffectedFileLink(issue, StringUtils.EMPTY);
    }

    /**
     * Returns an HTML link that references the UI representation of the affected file of the specified issue.
     *
     * @param issue
     *         the issue to create the link for
     * @param prefix
     *         prefix to the file name URL
     *
     * @return the link (if the file is accessible)
     */
    public DomContent createAffectedFileLink(final Issue issue, final String prefix) {
        if (ConsoleLogHandler.isInConsoleLog(issue.getFileName())) {
            return a().withHref(prefix + getSourceCodeUrl(issue))
                    .withText(getFileNameAtLine(issue));
        }
        else if (facade.canAccessAffectedFileOf(build, issue)) {
            return a().withHref(prefix + getSourceCodeUrl(issue))
                    .withText(getFileNameAtLine(issue))
                    .attr("data-bs-toggle", "tooltip")
                    .attr("data-bs-placement", "top")
                    .withTitle(issue.getFileName());
        }
        else {
            return text(getFileNameAtLine(issue));
        }
    }

    /**
     * Returns the URL to show the source code with the affected issue line.
     *
     * @param issue
     *         the issue to show the source code for
     *
     * @return the URL
     */
    public String getSourceCodeUrl(final Issue issue) {
        return "source.%s/#%d".formatted(issue.getId(), issue.getLineStart());
    }

    /**
     * Returns the base name of the affected file of the specified issue (including the specified line number).
     *
     * @param issue
     *         the issue to get the base name for
     *
     * @return the file name
     */
    public String getFileNameAtLine(final Issue issue) {
        return "%s:%d".formatted(getFileName(issue), issue.getLineStart());
    }

    /**
     * Returns the base name of the affected file of the specified issue.
     *
     * @param issue
     *         the issue to get the base name for
     *
     * @return the file name
     */
    public String getFileName(final Issue issue) {
        if (ConsoleLogHandler.isInConsoleLog(issue.getFileName())) {
            return Messages.ConsoleLog_Name();
        }
        else {
            return issue.getBaseName();
        }
    }
}
