package io.jenkins.plugins.analysis.core.testutil;

import org.eclipse.collections.impl.factory.Lists;

import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ToolSelection;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;

import static org.mockito.Mockito.*;

/**
 * Creates job stubs that contain static analysis results.
 *
 * @author Ullrich Hafner
 * @see Job
 * @see JobAction
 * @see ResultAction
 */
@SuppressWarnings("rawtypes")
public final class JobStubs {
    /** ID of SpotBugs actions. */
    public static final String SPOT_BUGS_ID = "spotbugs";
    /** Name of SpotBugs actions. */
    public static final String SPOT_BUGS_NAME = "SpotBugs";

    private static final String IMAGE_SUFFIX = ".png";
    /** Name of SpotBugs icon. */
    public static final String SPOT_BUGS_ICON = SPOT_BUGS_ID + IMAGE_SUFFIX;
    /** ID of CheckStyle actions. */
    public static final String CHECK_STYLE_ID = "checkstyle";
    /** Name of CheckStyle actions. */
    public static final String CHECK_STYLE_NAME = "CheckStyle";
    /** Name of CheckStyle icon. */
    public static final String CHECK_STYLE_ICON = CHECK_STYLE_ID + IMAGE_SUFFIX;

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
        when(tool.getSmallIconUrl()).thenReturn(id + IMAGE_SUFFIX);
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
    public static Job<?, ?> createJobWithActions(final ResultAction... actions) {
        Job job = createJob();

        Run<?, ?> build = createBuildWithActions(actions);
        when(job.getLastCompletedBuild()).thenReturn(build);

        return job;
    }

    private static Job createJob() {
        return mock(Job.class);
    }

    /**
     * Creates a stub for a {@link Run} that has the specified actions attached.
     *
     * @param actions
     *         the actions to attach, might be empty
     *
     * @return the run stub
     */
    public static Run<?, ?> createBuildWithActions(final ResultAction... actions) {
        Run<?, ?> build = createBuild();

        when(build.getActions(ResultAction.class)).thenReturn(Lists.fixedSize.of(actions));

        return build;
    }

    private static Run createBuild() {
        return mock(Run.class);
    }

    /**
     * Creates a stub for a {@link Job} that has a single static analysis action attached.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     * @param totalSize
     *         the total number of issues for the tool
     *
     * @return the {@link Job} stub
     */
    public static Job<?, ?> createJob(final String id, final String name, final int totalSize) {
        return createJobWithActions(createAction(id, name, totalSize));
    }

    /**
     * Creates a stub for a static analysis {@link ResultAction}.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     * @param totalSize
     *         the total number of issues for the tool
     * @param newSize
     *         the total number of new issues for the tool
     * @param fixedSize
     *         the total number of fixed issues for the tool
     *
     * @return the {@link ResultAction} stub
     */
    public static ResultAction createAction(final String id, final String name,
            final int totalSize, final int newSize, final int fixedSize) {
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(totalSize);
        when(result.getNewSize()).thenReturn(newSize);
        when(result.getFixedSize()).thenReturn(fixedSize);
        when(result.getTotals()).thenReturn(
                new IssuesStatisticsBuilder()
                        .setTotalNormalSize(totalSize)
                        .setNewNormalSize(newSize)
                        .setFixedSize(fixedSize)
                        .build());

        return createAction(id, name, result);
    }

    /**
     * Creates a stub for a static analysis {@link ResultAction}.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         the name of the static analysis tool
     * @param totalSize
     *         the total number of issues for the tool
     *
     * @return the {@link ResultAction} stub
     */
    public static ResultAction createAction(final String id, final String name, final int totalSize) {
        return createAction(id, name, totalSize, 0, 0);
    }

    /**
     * Creates a stub for a static analysis {@link ResultAction}.
     *
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         name of the static analysis tool
     * @param result
     *         the result to attach
     *
     * @return the {@link ResultAction} stub
     */
    @SuppressWarnings("unchecked")
    public static ResultAction createAction(final String id, final String name, final AnalysisResult result) {
        ResultAction resultAction = mock(ResultAction.class);

        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getName()).thenReturn(name);
        when(resultAction.getRelativeUrl()).thenReturn(url(id));
        when(resultAction.getUrlName()).thenReturn(id);
        Run build = createBuild();
        when(resultAction.getOwner()).thenReturn(build);

        return resultAction;
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
        var toolSelection = new ToolSelection();
        toolSelection.setId(id);
        return toolSelection;
    }

    private JobStubs() {
        // prevents instantiation
    }
}
