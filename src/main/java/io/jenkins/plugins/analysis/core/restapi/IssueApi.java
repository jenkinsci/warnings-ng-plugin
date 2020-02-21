package io.jenkins.plugins.analysis.core.restapi;

import edu.hm.hafner.analysis.Issue;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.forensics.blame.FileBlame;

/**
 * Remote API for an {@link Issue}. Simple Java Bean that exposes several methods of an {@link Issue} instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class IssueApi {
    private final Issue issue;
    private final FileBlame fileBlame;

    /**
     * Creates a new {@link IssueApi}.
     *
     * @param issue
     *         the issue to expose the properties from
     * @param fileBlame
     *          the blame of the issue file
     */
    public IssueApi(final Issue issue, @Nullable final FileBlame fileBlame) {
        this.issue = issue;
        this.fileBlame = fileBlame;
    }

    @Exported
    public String getFileName() {
        return issue.getFileName();
    }

    @Exported
    public String getBaseName() {
        return issue.getBaseName();
    }

    @Exported
    public String getCategory() {
        return issue.getCategory();
    }

    @Exported
    public String getType() {
        return issue.getType();
    }

    @Exported
    public String getSeverity() {
        return issue.getSeverity().getName();
    }

    @Exported
    public String getMessage() {
        return issue.getMessage();
    }

    @Exported
    public String getDescription() {
        return issue.getDescription();
    }

    @Exported
    public int getLineStart() {
        return issue.getLineStart();
    }

    @Exported
    public int getLineEnd() {
        return issue.getLineEnd();
    }

    @Exported
    public int getColumnStart() {
        return issue.getColumnStart();
    }

    @Exported
    public int getColumnEnd() {
        return issue.getColumnEnd();
    }

    @Exported
    public String getPackageName() {
        return issue.getPackageName();
    }

    @Exported
    public String getModuleName() {
        return issue.getModuleName();
    }

    @Exported
    public String getOrigin() {
        return issue.getOrigin();
    }

    @Exported
    public String getReference() {
        return issue.getReference();
    }

    @Exported
    public String getFingerprint() {
        return issue.getFingerprint();
    }

    /**
     * Returns the author's name of the blame.
     *
     * @return the name of the author
     */
    @Exported
    public String getAuthor() {
        if (fileBlame != null) {
            return fileBlame.getName(getLineStart());
        }
        return "";
    }

    /**
     * Returns the author's email of the blame.
     *
     * @return the email of the author
     */
    @Exported
    public String getEmail() {
        if (fileBlame != null) {
            return fileBlame.getEmail(getLineStart());
        }
        return "";
    }

    /**
     * Returns the commit's sha1 of the blame.
     *
     * @return the commit's sha1 of the blame
     */
    @Exported
    public String getCommit() {
        if (fileBlame != null) {
            return fileBlame.getCommit(getLineStart());
        }
        return "";
    }
}
