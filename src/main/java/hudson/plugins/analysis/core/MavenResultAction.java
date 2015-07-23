package hudson.plugins.analysis.core;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import hudson.FilePath;
import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;

import hudson.model.Run;
import hudson.model.HealthReport;
import hudson.model.Result;

import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.StringPluginLogger;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.plugins.analysis.util.model.AbstractAnnotation;

/**
 * Base class for Maven aggregated build reports.
 *
 * @author Ulli Hafner
 * @since 1.20
 * @param <T> type of the build result
 */
public abstract class MavenResultAction<T extends BuildResult> implements StaplerProxy, AggregatableAction, MavenAggregatedReport, ResultAction<T> {
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;
    /** Reuse all the functionality of the action for freestyle jobs. */
    private final AbstractResultAction<T> delegate;

    private transient StringPluginLogger logger;
    private transient Set<MavenModule> modules = Sets.newHashSet();
    private final transient String pluginName;

    /**
     * Creates a new instance of {@link MavenResultAction}.
     *
     * @param delegate
     *            result action for freestyle jobs that will do the main of the
     *            work
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param pluginName
     *            name of the plug-in
     */
    public MavenResultAction(final AbstractResultAction<T> delegate, final String defaultEncoding, final String pluginName) {
        this.defaultEncoding = defaultEncoding;
        this.delegate = delegate;
        this.pluginName = pluginName;
    }

    @Override
    public abstract Class<? extends MavenResultAction<T>> getIndividualActionType();

    /**
     * Returns the URL of the 24x24 image used in the build link.
     *
     * @return the URL of the image
     * @since 1.42
     */
    public String getSmallImageName() {
        return delegate.getSmallImageName();
    }

    /**
     * Creates a new build result that contains the aggregated results.
     *
     * @param existingResult
     *            the already existing result
     * @param additionalResult
     *            the additional result to be aggregated with the existing
     *            result
     * @return the created result
     */
    protected abstract T createResult(T existingResult, T additionalResult);

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
    @Override
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        MavenResultAction<T> additionalAction = newBuild.getAction(getIndividualActionType());
        MavenModule project = newBuild.getProject();
        if (additionalAction != null && !getModules().contains(project)) {
            T existingResult = delegate.getResult();
            T additionalResult = additionalAction.getResult();

            if (newBuild.getResult().isBetterThan(Result.FAILURE) || additionalResult.getPluginResult().isWorseOrEqualTo(Result.FAILURE)) {
                getModules().add(project);
                setResult(createAggregatedResult(existingResult, additionalResult));

                copySourceFilesToModuleBuildFolder(newBuild);
            }
        }
    }

    private void copySourceFilesToModuleBuildFolder(final MavenBuild newBuild) {
        FilePath filePath = new FilePath(new File(newBuild.getRootDir(), AbstractAnnotation.WORKSPACE_FILES));
        try {
            filePath.copyRecursiveTo("*.tmp", new FilePath(new File(getOwner().getRootDir(), AbstractAnnotation.WORKSPACE_FILES)));
        }
        catch (IOException exception) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Can't copy workspace files: ", exception);
        }
        catch (InterruptedException exception) {
            // ignore, user canceled the operation
        }
    }

    private T createAggregatedResult(@CheckForNull final T existingResult, final T additionalResult) {
        T createdResult = createResult(existingResult, additionalResult);
        if (new NullHealthDescriptor(delegate.getHealthDescriptor()).isThresholdEnabled()) {
            createdResult.evaluateStatus(additionalResult.getThresholds(), additionalResult.canUseDeltaValues(),
                    getLogger(), getUrlName());
        }
        return createdResult;
    }

    /**
     * Aggregates the results in a new instance of {@link ParserResult}.
     *
     * @param existingResult
     *            an already existing result, might be <code>null</code> for the
     *            first aggregation
     * @param additionalResult
     *            the additional result to be aggregated with the existing
     *            result
     * @return the aggregated result
     */
    protected ParserResult aggregate(@CheckForNull final T existingResult, final T additionalResult) {
        ParserResult aggregatedAnnotations = new ParserResult();

        List<BuildResult> results = Lists.newArrayList();
        if (existingResult != null) {
            results.add(existingResult);
        }
        results.add(additionalResult);

        for (BuildResult result : results) {
            aggregatedAnnotations.addAnnotations(result.getAnnotations());
            aggregatedAnnotations.addModules(result.getModules());
            aggregatedAnnotations.addErrors(result.getErrors());
        }

        return aggregatedAnnotations;
    }

    private PluginLogger getLogger() {
        if (logger == null) {
            logger = createLogger();
        }
        return logger;
    }

    private StringPluginLogger createLogger() {
        return new StringPluginLogger("[" + StringUtils.defaultString(pluginName, "ANALYSIS") + "] "); // NOCHECKSTYLE
    }

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

    /**
     * Returns whether a large image is defined.
     *
     * @return <code>true</code> if a large image is defined, <code>false</code>
     *         otherwise. If no large image is defined, then the attribute
     *         {@code icon} must to be provided in jelly tag {@code summary}.
     * @since 1.41
     */
    public boolean hasLargeImage() {
        return delegate.hasLargeImage();
    }

    /**
     * Returns the URL of the 48x48 image used in the build summary.
     *
     * @return the URL of the image
     * @since 1.41
     */
    public String getLargeImageName() {
        return delegate.getLargeImageName();
    }

    @Override
    public String getIconFileName() {
        return delegate.getIconFileName();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public String getUrlName() {
        return delegate.getUrlName();
    }

    // CHECKSTYLE:OFF
    @Override
    public void setResult(final T additionalResult) {
        delegate.setResult(additionalResult);
    }

    @Override
    public T getResult() {
        return delegate.getResult();
    }

    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @since 1.73
     * @return <code>true</code> if only stable builds should be used
     */
    public boolean useOnlyStableBuildsAsReference() {
        return delegate.getResult().useOnlyStableBuildsAsReference();
    }

    /**
     * Determines whether to always use the previous build as the reference.
     *
     * @since 1.73
     * @return <code>true</code> if the previous build should always be used.
     */
    public boolean usePreviousBuildAsStable() {
        return delegate.getResult().usePreviousBuildAsStable();
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public Run<?, ?> getOwner() {
        return delegate.getOwner();
    }

    /**
     * Returns the health of this action.
     *
     * @return the health of this action
     */
    public final HealthReport getBuildHealth() {
        return delegate.getBuildHealth();
    }

    @Override
    public ToolTipProvider getToolTipProvider() {
        return delegate.getToolTipProvider();
    }

    @Override
    public final Run<?, ?> getBuild() {
        return delegate.getBuild();
    }

    @Override
    public final Object getTarget() {
        return delegate.getTarget();
    }

    /**
     * Returns the tooltip for the specified number of items.
     *
     * @param numberOfItems
     *            the number of items to display the tooltip for
     * @return the tooltip for the specified items
     */
    public String getTooltip(final int numberOfItems) {
        return delegate.getTooltip(numberOfItems);
    }

    @Override
    public boolean isSuccessful() {
        return delegate.isSuccessful();
    }

    @Override
    public AbstractHealthDescriptor getHealthDescriptor() {
        return delegate.getHealthDescriptor();
    }
    // CHECKSTYLE:ON
}
