package hudson.plugins.analysis.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;

import hudson.model.HealthReport;
import hudson.model.AbstractBuild;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.StringPluginLogger;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Base class for Maven aggregated build reports.
 *
 * @author Ulli Hafner
 * @since 1.20
 * @param <T> type of the build result
 */
public abstract class MavenResultAction<T extends BuildResult> implements AggregatableAction, MavenAggregatedReport {
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;
    /** Reuse all the functionality of the action for freestyle jobs. */
    private final AbstractResultAction<T> delegate;

    private transient StringPluginLogger logger = createLogger();
    private transient Set<MavenModule> modules = Sets.newHashSet();

    private StringPluginLogger createLogger() {
        return new StringPluginLogger("[" + StringUtils.upperCase(getDisplayName()) + "] "); //NOCHECKSTYLE
    }

    /**
     * Creates a new instance of {@link MavenResultAction}.
     *
     * @param delegate
     *            result action for freestyle jobs that will do the main of the
     *            work
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public MavenResultAction(final AbstractResultAction<T> delegate, final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    public abstract Class<? extends MavenResultAction<T>> getIndividualActionType();

    /**
     * Creates a new build result that contains the aggregated annotations.
     *
     * @param existingResult
     *            the existing result
     * @param aggregatedAnnotations
     *            the aggregated annotations
     * @return the created result
     */
    protected abstract T createResult(final T existingResult, ParserResult aggregatedAnnotations);

    /**
     * Called whenever a new module build is completed, to update the aggregated
     * report. When multiple builds complete simultaneously, Jenkins serializes
     * the execution of this method, so this method needs not be
     * concurrency-safe.
     *
     * @param moduleBuilds
     *            Same as <tt>MavenModuleSet.getModuleBuilds()</tt> but provided
     *            for convenience and efficiency.
     * @param newBuild
     *            Newly completed build.
     */
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        MavenResultAction<T> additionalAction = newBuild.getAction(getIndividualActionType());
        MavenModule project = newBuild.getProject();
        if (additionalAction != null && !getModules().contains(project)) {
            getModules().add(project);
            getLogger().log("Aggregating results of " + project.getDisplayName());

            T existingResult = delegate.getResult();
            T additionalResult = additionalAction.getResult();

            if (existingResult == null) {
                setResult(additionalResult);
                getOwner().setResult(additionalResult.getPluginResult());
            }
            else {
                setResult(aggregate(existingResult, additionalResult));
            }
        }
    }

    private T aggregate(final T existingResult, final T additionalResult) {
        ParserResult aggregatedAnnotations = new ParserResult();
        aggregatedAnnotations.addAnnotations(existingResult.getAnnotations());
        aggregatedAnnotations.addAnnotations(additionalResult.getAnnotations());

        T createdResult = createResult(existingResult, aggregatedAnnotations);
        createdResult.evaluateStatus(existingResult.getThresholds(), existingResult.canUseDeltaValues(), getLogger());
        return createdResult;
    }

    private PluginLogger getLogger() {
        if (logger == null) {
            logger = createLogger();
        }
        return logger;
    }

    /**
     * Returns the modules.
     *
     * @return the modules
     */
    private Set<MavenModule> getModules() {
        if (modules == null) {
            modules = Sets.newHashSet();
        }
        return modules;
    }

    /**
     * Returns all logging statements of this action that couldn't be printed so far.
     *
     * @return the logging statements
     */
    public String getLog() {
        String message = getLogger().toString();
        logger = createLogger();
        return message;
    }

    /**
     * Returns the default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        return delegate.getIconFileName();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return delegate.getUrlName();
    }

    // CHECKSTYLE:OFF
    private void setResult(final T additionalResult) {
        delegate.setResult(additionalResult);
    }

    public T getResult() {
        return delegate.getResult();
    }

    public AbstractBuild<?, ?> getOwner() {
        return delegate.getOwner();
    }

    public final HealthReport getBuildHealth() {
        return delegate.getBuildHealth();
    }

    public ToolTipProvider getToolTipProvider() {
        return delegate.getToolTipProvider();
    }

    public final AbstractBuild<?, ?> getBuild() {
        return delegate.getBuild();
    }

    public final Object getTarget() {
        return delegate.getTarget();
    }

    public String getTooltip(final int numberOfItems) {
        return delegate.getTooltip(numberOfItems);
    }

    public boolean isSuccessful() {
        return delegate.isSuccessful();
    }

    public HealthDescriptor getHealthDescriptor() {
        return delegate.getHealthDescriptor();
    }
    // CHECKSTYLE:ON
}
