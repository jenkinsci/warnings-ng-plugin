package io.jenkins.plugins.analysis.core.restapi;

import edu.hm.hafner.analysis.Issue;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import io.jenkins.plugins.analysis.core.util.Blame;

/**
 * Remote API for an {@link Issue}. Simple Java Bean that exposes several methods of an {@link Issue} instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class IssueApi {
    private final Issue issue;
    private final Blame blame;
    private final List<String> sourceDirectories;

    /**
     * Creates a new {@link IssueApi}.
     *
     * @param issue
     *         the issue to expose the properties from
     * @param blame
     *         the blame which contains this issue
     * @param sourceDirectories
     *         list of configured source directories
     */
    public IssueApi(final Issue issue, final Blame blame, final List<String> sourceDirectories) {
        this.issue = issue;
        this.blame = blame;
        this.sourceDirectories = new ArrayList<>(sourceDirectories);
    }

    @Exported
    @Whitelisted
    public String getFileName() {
        String fileName = issue.getFileName();
        
        if (sourceDirectories.isEmpty()) {
            return fileName;
        }
        
        String normalizedFileName = fileName.replace("\\", "/");
        
        for (String sourceDir : sourceDirectories) {
            String normalizedSourceDir = sourceDir.replace("\\", "/");
            
            if (!normalizedSourceDir.isEmpty() && !normalizedSourceDir.endsWith("/")) {
                normalizedSourceDir += "/";
            }
            
            if (normalizedFileName.startsWith(normalizedSourceDir)) {
                return normalizedFileName.substring(normalizedSourceDir.length());
            }
        }
        
        return fileName;
    }

    @Exported
    @Whitelisted
    public String getBaseName() {
        return issue.getBaseName();
    }

    @Exported
    @Whitelisted
    public String getCategory() {
        return issue.getCategory();
    }

    @Exported
    @Whitelisted
    public String getType() {
        return issue.getType();
    }

    @Exported
    @Whitelisted
    public String getSeverity() {
        return issue.getSeverity().getName();
    }

    @Exported
    @Whitelisted
    public String getMessage() {
        return issue.getMessage();
    }

    @Exported
    @Whitelisted
    public String getDescription() {
        return issue.getDescription();
    }

    @Exported
    @Whitelisted
    public int getLineStart() {
        return issue.getLineStart();
    }

    @Exported
    @Whitelisted
    public int getLineEnd() {
        return issue.getLineEnd();
    }

    @Exported
    @Whitelisted
    public int getColumnStart() {
        return issue.getColumnStart();
    }

    @Exported
    @Whitelisted
    public int getColumnEnd() {
        return issue.getColumnEnd();
    }

    @Exported
    @Whitelisted
    public String getPackageName() {
        return issue.getPackageName();
    }

    @Exported
    @Whitelisted
    public String getModuleName() {
        return issue.getModuleName();
    }

    @Exported
    @Whitelisted
    public String getOrigin() {
        return issue.getOrigin();
    }

    @Exported
    @Whitelisted
    public String getOriginName() {
        return issue.getOriginName();
    }

    @Exported
    @Whitelisted
    public String getReference() {
        return issue.getReference();
    }

    @Exported
    @Whitelisted
    public String getFingerprint() {
        return issue.getFingerprint();
    }

    @Exported
    @Whitelisted
    public String getAuthorName() {
        return blame.getAuthorName();
    }

    @Exported
    @Whitelisted
    public String getAuthorEmail() {
        return blame.getAuthorEmail();
    }

    @Exported
    @Whitelisted
    public String getCommit() {
        return blame.getCommit();
    }

    @Exported
    @Whitelisted
    public int getAddedAt() {
        return blame.getAddedAt();
    }

    @Exported
    @Whitelisted
    @Override
    public String toString() {
        return issue.toString();
    }
}
