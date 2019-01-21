package io.jenkins.plugins.analysis.core.testutil;

import java.util.Optional;

import org.eclipse.collections.impl.factory.Lists;

import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ToolSelection;

import static org.mockito.Mockito.*;

/**
 * Creates job stubs that contain static analysis results.
 *
 * @author Ullrich Hafner
 * @see Job
 * @see JobAction
 * @see ResultAction
 */
public final class JobStubs {
    /** ID of SpotBugs actions. */
    public static final String SPOT_BUGS_ID = "spotbugs";
    /** Name of SpotBugs actions. */
    public static final String SPOT_BUGS_NAME = "SpotBugs";
    /** ID of CheckStyle actions. */
    public static final String CHECK_STYLE_ID = "checkstyle";
    /** Name of CheckStyle actions. */
    public static final String CHECK_STYLE_NAME = "CheckStyle";

    /**
     * Registers a stub for the specified tool in the label provider factory so that this tool later on will be picked
     * up by the job actions. This stub provides ID, names, and icon.
     *
     * @param factory
     *         the factory to register the action for
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     */
    public static void registerTool(final LabelProviderFactory factory, final String id, final String name) {
        StaticAnalysisLabelProvider tool = mock(StaticAnalysisLabelProvider.class);
        when(factory.create(id, name)).thenReturn(tool);
        when(factory.create(id)).thenReturn(tool);
        when(tool.getSmallIconUrl()).thenReturn(id + ".png");
        when(tool.getName()).thenReturn(name);
        when(tool.getLinkName()).thenReturn(name);
    }

    /**
     * Creates a synthetic URL to the job results of the specified static analysis tool.
     *
     * @param id
     *         the ID of the static analysis tool
     *
     * @return the URL
     */
    public static String url(final String id) {
        return "job/build/" + id;
    }

    /**
     * Creates a stub for a {@link Job} that has the specified actions attached.
     *
     * @param actions
     *         the actions to attach, might be empty
     *
     * @return the job stub
     */
    public static Job createJobWithActions(final JobAction... actions) {
        Job job = mock(Job.class);

        when(job.getActions(JobAction.class)).thenReturn(Lists.fixedSize.of(actions));

        return job;
    }

    /**
     * Creates a stub for a {@link Job} that has a single static analysis action attached.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     * @param size
     *         the total number of issues for the tool
     *
     * @return the {@link Job} stub
     */
    public static Job<?, ?> createJob(final String id, final String name, final int size) {
        return createJobWithActions(createAction(size, id, name));
    }

    /**
     * Creates a stub for a static analysis {@link JobAction}.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     * @param size
     *         the total number of issues for the tool
     *
     * @return the {@link JobAction} stub
     */
    public static JobAction createAction(final int size, final String id, final String name) {
        JobAction jobAction = mock(JobAction.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(jobAction.getLatestAction()).thenReturn(Optional.of(resultAction));
        when(jobAction.getId()).thenReturn(id);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(size);

        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getName()).thenReturn(name);
        when(resultAction.getRelativeUrl()).thenReturn(url(id));
        when(resultAction.getUrlName()).thenReturn(id);

        return jobAction;
    }

    /**
     * Creates a new {@link ToolSelection} for the specified ID.
     *
     * @param id
     *         the ID of the static analysis tool
     *
     * @return the tool selection
     */
    public static ToolSelection createTool(final String id) {
        ToolSelection toolSelection = new ToolSelection();
        toolSelection.setId(id);
        return toolSelection;
    }

    private JobStubs() {
        // prevents instantiation
    }
}
