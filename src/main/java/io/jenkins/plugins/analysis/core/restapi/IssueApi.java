package io.jenkins.plugins.analysis.core.restapi;

import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;

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
     */
    public IssueApi(final Issue issue) {
        this.issue = issue;
        this.fileBlame = null;
    }
    /**
     * Creates a new {@link IssueApi}.
     *
     * @param issue
     *         the issue to expose the properties from
     * @param fileBlame
     *          the blame of the issue file
     */
    public IssueApi(final Issue issue, final FileBlame fileBlame) {
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
     * Creates {@link BlameApi} for this issue. Note that an issue contains
     * multiple lines may contain multiple blames
     *
     * @return The BlameApi(s) for this issue
     */
    @Exported(inline = true)
    public List<BlameApi> getBlames() {
        if (fileBlame == null) {
            return null;
        }
        return fileBlame.getLines().stream()
                .filter(line -> line >= getLineStart() && line <= getLineEnd())
                .map(line -> new BlameApi(fileBlame, line))
                .collect(Collectors.toList());
    }
}
