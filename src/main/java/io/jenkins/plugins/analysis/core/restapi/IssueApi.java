package io.jenkins.plugins.analysis.core.restapi;

import edu.hm.hafner.analysis.Issue;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Remote API for an {@link Issue}. Simple Java Bean that exposes several methods of an {@link Issue} instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class IssueApi {
    private final Issue issue;

    /**
     * Creates a new {@link IssueApi}.
     *
     * @param issue
     *         the issue to expose the properties from
     */
    public IssueApi(final Issue issue) {
        this.issue = issue;
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
}
