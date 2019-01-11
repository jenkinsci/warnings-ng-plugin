package io.jenkins.plugins.analysis.core.portlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesTablePortlet}.
 *
 * @author Ullrich Hafner
 */
class IssuesTablePortletTest {
    private static final String SPOT_BUGS_ID = "spotbugs";
    private static final String SPOT_BUGS_NAME = "SpotBugs Warnings";
    private static final String CHECK_STYLE_ID = "checkstyle";
    private static final String CHECK_STYLE_NAME = "CheckStyle Warnings";

    @Test
    void shouldThrowExceptionIfToolNamesAreNotSet() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        assertThatIllegalStateException()
                .as("Mapping is generated in getToolNames and must be called first")
                .isThrownBy(() -> issues.getTotals(mock(Job.class)));
    }

    @Test
    void shouldShowTableWithOneJob() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        List<Job<?, ?>> jobs = createJobs(createJob(1, SPOT_BUGS_ID, SPOT_BUGS_NAME));

        assertThat(issues.getToolNames(jobs)).containsExactly(SPOT_BUGS_NAME);
        assertThat(issues.getTotals(jobs.get(0))).containsExactly("1");
    }

    @Test
    void shouldShowTableWithTwoJobs() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        List<Job<?, ?>> jobs = createJobs(createJob(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createJob(2, SPOT_BUGS_ID, SPOT_BUGS_NAME));

        assertThat(issues.getToolNames(jobs)).containsExactly(SPOT_BUGS_NAME);
        assertThat(issues.getTotals(jobs.get(0))).containsExactly("1");
        assertThat(issues.getTotals(jobs.get(1))).containsExactly("2");
    }

    @Test
    void shouldShowTableWithTwoTools() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        List<Job<?, ?>> jobs = createJobs(createJobWithActions(createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME)));

        assertThat(issues.getToolNames(jobs)).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);
        assertThat(issues.getTotals(jobs.get(0))).containsExactly("2", "1");
    }

    @Test
    void shouldShowTableWithTwoToolsAndTwoJobs() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        List<Job<?, ?>> jobs = new ArrayList<>();
        Job first = createJobWithActions(createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        jobs.add(first);
        Job second = createJobWithActions(createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        jobs.add(second);

        assertThat(issues.getToolNames(jobs)).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);
        assertThat(issues.getVisibleJobs(jobs)).containsExactly(first, second);
        assertThat(issues.getTotals(first)).containsExactly("2", "1");
        assertThat(issues.getTotals(second)).containsExactly("4", "3");
    }

    @Test
    void shouldFilterZeroIssuesJobs() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");
        issues.setHideCleanJobs(true);

        List<Job<?, ?>> jobs = new ArrayList<>();
        Job first = createJobWithActions(createAction(0, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(0, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        jobs.add(first);
        Job second = createJobWithActions(createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        jobs.add(second);

        assertThat(issues.getToolNames(jobs)).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);
        assertThat(issues.getVisibleJobs(jobs)).containsExactly(second);
    }

    @Test
    void shouldFilterNonActionJobs() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");
        issues.setHideCleanJobs(true);

        List<Job<?, ?>> jobs = new ArrayList<>();
        Job first = createJobWithActions();
        jobs.add(first);
        Job second = createJobWithActions(createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        jobs.add(second);

        assertThat(issues.getToolNames(jobs)).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);
        assertThat(issues.getVisibleJobs(jobs)).containsExactly(second);
    }

    @Test
    void shouldShowTableWithTwoJobsWithDifferentTools() {
        IssuesTablePortlet issues = new IssuesTablePortlet("issues");

        List<Job<?, ?>> jobs = new ArrayList<>();

        Job first = createJobWithActions(createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME));
        jobs.add(first);

        Job second = createJobWithActions(createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        jobs.add(second);

        assertThat(issues.getToolNames(jobs)).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);
        assertThat(issues.getTotals(first)).containsExactly("-", "1");
        assertThat(issues.getTotals(second)).containsExactly("2", "-");
    }

    private List<Job<?, ?>> createJobs(final Job<?, ?>... analysisJobs) {
        List<Job<?, ?>> jobs = new ArrayList<>();
        Collections.addAll(jobs, analysisJobs);
        return jobs;
    }

    private Job createJobWithActions(final JobAction... actions) {
        Job job = mock(Job.class);

        when(job.getActions(JobAction.class)).thenReturn(Lists.fixedSize.of(actions));

        return job;
    }

    private Job<?, ?> createJob(final int size, final String id, final String name) {
        Job job = mock(Job.class);
        JobAction jobAction = createAction(size, id, name);

        when(job.getActions(JobAction.class)).thenReturn(Collections.singletonList(jobAction));

        return job;
    }

    private JobAction createAction(final int size, final String id, final String name) {
        JobAction jobAction = mock(JobAction.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(jobAction.getLatestAction()).thenReturn(Optional.of(resultAction));
        when(jobAction.getId()).thenReturn(id);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(size);

        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getDisplayName()).thenReturn(name);
        return jobAction;
    }
}