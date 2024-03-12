package io.jenkins.plugins.analysis.warnings;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.SnippetGenerator;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

/**
 * Ehances thbe {@link PageObject} for the {@link SnippetGenerator} with the steps of the warnings plugin.
 *
 * @author Lion Kosiuk
 */
public class WarningsSnippetGenerator extends SnippetGenerator {
    private static final String RECORD_ISSUES_OPTION = "recordIssues: Record compiler warnings and static analysis results";
    private final Control selectSampleStep = control("/");

    /**
     * Creates a new page object.
     *
     * @param context
     *         job context
     */
    public WarningsSnippetGenerator(final WorkflowJob context) {
        super(context);
    }

    /**
     * Set the sample step of the WarningsSnippetGenerator to record Issues.
     *
     * @return issuesRecorder
     */
    public IssuesRecorder selectRecordIssues() {
        selectSampleStep.select(RECORD_ISSUES_OPTION);
        elasticSleep(2000);
        return new IssuesRecorder(getContext(), "/prototype");
    }
}
