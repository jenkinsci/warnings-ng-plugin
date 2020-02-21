package io.jenkins.plugins.analysis.core.restapi;

import edu.hm.hafner.analysis.Issue;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

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

    @Exported @Whitelisted
    public String getFileName() {
        return issue.getFileName();
    }

    @Exported @Whitelisted
    public String getBaseName() {
        return issue.getBaseName();
    }

    @Exported @Whitelisted
    public String getCategory() {
        return issue.getCategory();
    }

    @Exported @Whitelisted
    public String getType() {
        return issue.getType();
    }

    @Exported @Whitelisted
    public String getSeverity() {
        return issue.getSeverity().getName();
    }

    @Exported @Whitelisted
    public String getMessage() {
        return issue.getMessage();
    }

    @Exported @Whitelisted
    public String getDescription() {
        return issue.getDescription();
    }

    @Exported @Whitelisted
    public int getLineStart() {
        return issue.getLineStart();
    }

    @Exported @Whitelisted
    public int getLineEnd() {
        return issue.getLineEnd();
    }

    @Exported @Whitelisted
    public int getColumnStart() {
        return issue.getColumnStart();
    }

    @Exported @Whitelisted
    public int getColumnEnd() {
        return issue.getColumnEnd();
    }

    @Exported @Whitelisted
    public String getPackageName() {
        return issue.getPackageName();
    }

    @Exported @Whitelisted
    public String getModuleName() {
        return issue.getModuleName();
    }

    @Exported @Whitelisted
    public String getOrigin() {
        return issue.getOrigin();
    }

    @Exported @Whitelisted
    public String getReference() {
        return issue.getReference();
    }

    @Exported @Whitelisted
    public String getFingerprint() {
        return issue.getFingerprint();
    }

    @Exported @Whitelisted
    @Override
    public String toString() {
        return issue.toString();
    }
}
