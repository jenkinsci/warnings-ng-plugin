package hudson.plugins.analysis.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.model.AbstractBuild;
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
 * @param <T>
 *            type of the result of this action
 * @author Ulli Hafner
 */
//CHECKSTYLE:COUPLING-OFF
@ExportedBean
public abstract class AbstractResultAction<T extends BuildResult> implements StaplerProxy, HealthReportingAction, ToolTipProvider, ResultAction<T>, LastBuildAction {
    /** The associated run of this action. */
    private final Run<?, ?> owner;
    /** Parameters for the health report. */
    private final AbstractHealthDescriptor healthDescriptor;
    /** The actual result of this action. */
    private T result;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor
     * @param result
     *            the result of the action
     */
    public AbstractResultAction(final Run<?, ?> owner, final AbstractHealthDescriptor healthDescriptor, final T result) {
        this.owner = owner;
        this.result = result;
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Returns the healthDescriptor.
     *
     * @return the healthDescriptor
     */
    @Override
    public AbstractHealthDescriptor getHealthDescriptor() {
        if (healthDescriptor == null) {
            return NullHealthDescriptor.NULL_HEALTH_DESCRIPTOR; // for old serialized actions
        }
        else {
            return healthDescriptor;
        }
    }

    /**
     * Returns the descriptor of the associated plug-in.
     *
     * @return the descriptor of the associated plug-in
     */
    protected abstract PluginDescriptor getDescriptor();

    @Exported
    public String getName() {
        return getDescriptor().getPluginName();
    }

    @Override
    public String getUrlName() {
        return getDescriptor().getPluginResultUrlName();
    }

    @Override @Exported
    public final HealthReport getBuildHealth() {
        return new HealthReportBuilder(getHealthDescriptor()).computeHealth(getResult());
    }

    @Override
    public ToolTipProvider getToolTipProvider() {
        return this;
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    @WithBridgeMethods(value=AbstractBuild.class, adapterMethod="getAbstractBuild")
    public final Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    @WithBridgeMethods(value=AbstractBuild.class, adapterMethod="getAbstractBuild")
    public final Run<?, ?> getBuild() {
        return owner;
    }

    /**
     * Returns the project actions if this action is used in a pipeline.
     *
     * @return default implementation returns empty collection, plug-in must override if they want to contribute to the UI
     */
    // FIXME: See JENKINS-31202. Currently the whole graphing is based around AbstractBuild (large refactoring required)
    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.emptyList();
    }

    /**
     * Added for backward compatibility. It generates <pre>AbstractBuild getOwner()</pre> bytecode during the build
     * process, so old implementations can use that signature.
     * 
     * @see {@link WithBridgeMethods}
     */
    @Deprecated
    private Object getAbstractBuild(final Run owner, final Class targetClass) {
      return owner instanceof AbstractBuild ? (AbstractBuild) owner : null;
    }

    @Override
    public final Object getTarget() {
        return getResult();
    }

    @Override @Exported
    public final T getResult() {
        return result;
    }

    @Override
    public final void setResult(final T result) {
        this.result = result;
    }

    @Override
    public String getIconFileName() {
        T currentResult = getResult();
        if (currentResult != null && currentResult.getNumberOfAnnotations() > 0) {
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
        return getDescriptor().getSummaryIconUrl();
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
        return createStaticIconUrl(getDescriptor().getIconUrl());
    }

    private String createStaticIconUrl(final String iconUrl) {
        return Jenkins.RESOURCE_PATH + "/" + iconUrl;
    }

    /**
     * Factory method to create the result of this action.
     *
     * @return the result of this action
     */
    protected ParserResult createResult() {
        return new ParserResult();
    }

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

    @Override @Exported
    public boolean isSuccessful() {
        return getResult().isSuccessful();
    }

    /**
     * Aggregates the results of the specified maven module builds.
     *
     * @param moduleBuilds
     *            the module builds to aggregate
     * @return the aggregated result
     * @deprecated replaced by {@link MavenResultAction}
     */
    @Deprecated
    protected ParserResult createAggregatedResult(final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new ParserResult();
    }

    /**
     * Updates the build status if the number of annotations exceeds one of the
     * thresholds.
     *
     * @param build
     *            the build to change the status from
     * @param buildResult
     *            the build result
     * @deprecated replaced by {@link MavenResultAction}
     */
    @Deprecated
    protected void updateBuildHealth(final MavenBuild build, final BuildResult buildResult) {
        // does nothing
    }

    /**
     * Adds a new module to the specified project. The new module is obtained
     * from the specified list of builds.
     *
     * @param aggregatedResult
     *            the result to add the module to
     * @param builds
     *            the builds for a module
     * @deprecated replaced by {@link MavenResultAction}
     */
    @Deprecated
    protected void addModule(final ParserResult aggregatedResult, final List<MavenBuild> builds) {
        // does nothing
    }

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor
     * @deprecated use
     *             {@link #AbstractResultAction(AbstractBuild, AbstractHealthDescriptor, BuildResult)}
     *             so that every action will have a result that is not null
     */
    @Deprecated
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final AbstractHealthDescriptor healthDescriptor) {
        this.owner = owner;
        this.healthDescriptor = healthDescriptor;
    }

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor
     * @param result
     *            the result of the action
     * @deprecated use {@link #AbstractResultAction(Run, AbstractHealthDescriptor, BuildResult)} instead
     */
    @Deprecated
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final AbstractHealthDescriptor healthDescriptor, final T result) {
        this.owner = owner;
        this.result = result;
        this.healthDescriptor = healthDescriptor;
    }

    /** Backward compatibility. @deprecated */
    @Deprecated
    @java.lang.SuppressWarnings("PMD")
    @SuppressFBWarnings("UuF")
    private transient HealthReportBuilder healthReportBuilder; // NOPMD
}
