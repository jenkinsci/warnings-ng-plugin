package io.jenkins.plugins.analysis.core.steps;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Controls the live cycle of the results in a job. This action persists the results
 * of a build and displays them on the build page. The actual visualization of
 * the results is defined in the matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the results trend.
 * </p>
 *
 * @author Ulli Hafner
 */
//CHECKSTYLE:COUPLING-OFF
@ExportedBean
public class PipelineResultAction implements StaplerProxy, HealthReportingAction, ToolTipProvider, LastBuildAction, Action {
    private final Run<?, ?> run;
    private AnalysisResult result;
    private String id;
    private HealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *  @param run
     *            the associated build of this action
     * @param result
     * @param healthDescriptor
     */
    public PipelineResultAction(final Run<?, ?> run, final AnalysisResult result, final String id,
                                final HealthDescriptor healthDescriptor) {
        this.run = run;
        this.result = result;
        this.id = id;
        this.healthDescriptor = healthDescriptor;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public String getId() {
        return id;
    }

    @Override @Exported
    public String getDisplayName() {
        return getIssueParser().getLinkName();
    }

    @Override
    public String getUrlName() {
        return getIssueParser().getResultUrl();
    }

    @Override @Exported
    public final HealthReport getBuildHealth() {
        return new HealthReportBuilder(healthDescriptor).computeHealth(getResult());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new ProjectAction(run.getParent(), id));
    }

    @Override
    public final Object getTarget() {
        return getResult();
    }

    @Exported
    public final AnalysisResult getResult() {
        return result;
    }

    @Override
    public String getIconFileName() {
        if (getResult().getNumberOfAnnotations() > 0) {
            return getSmallImage();
        }
        return null;
    }

    /**
     * Returns whether a large image is defined.
     *
     * @return <code>true</code> if a large image is defined, <code>false</code>
     *         otherwise. If no large image is defined, then the attribute
     *         {@code icon} must to be provided in jelly tag {@code summary}.
     * @since 1.41
     */
    public boolean hasLargeImage() {
        return StringUtils.isNotBlank(getLargeImageName());
    }

    /**
     * Returns the URL of the 48x48 image used in the build summary.
     *
     * @return the URL of the image
     * @since 1.41
     */
    public String getLargeImageName() {
        return getIssueParser().getLargeIconUrl();
    }

    /**
     * Returns the URL of the 24x24 image used in the build link.
     *
     * @return the URL of the image
     * @since 1.41
     */
    public String getSmallImageName() {
        return getSmallImage();
    }

    /**
     * Returns the URL of the 24x24 image used in the build link.
     *
     * @return the URL of the image
     */
    protected String getSmallImage() {
        return getIssueParser().getSmallIconUrl();
    }

    // FIXME: should be handled by IssuesParser
    @Override
    public String getTooltip(final int numberOfItems) {
        if (numberOfItems == 1) {
            return getSingleItemTooltip();
        }
        else {
            return getMultipleItemsTooltip(numberOfItems);
        }
    }

    /**
     * Returns the tooltip for several items.
     *
     * @param numberOfItems
     *            the number of items to display the tooltip for
     * @return the tooltip for several items
     */
    protected String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.ResultAction_MultipleWarnings(numberOfItems);
    }

    /**
     * Returns the tooltip for exactly one item.
     *
     * @return the tooltip for exactly one item
     */
    protected String getSingleItemTooltip() {
        return Messages.ResultAction_OneWarning();
    }

    @Exported
    public boolean isSuccessful() {
        return getResult().isSuccessful();
    }

    public IssueParser getIssueParser() {
        return IssueParser.find(id);
    }
}
