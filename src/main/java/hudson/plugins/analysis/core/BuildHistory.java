package hudson.plugins.analysis.core;

import javax.annotation.CheckForNull;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.Result;

import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * History of build results of a specific plug-in. The plug-in is identified by
 * the corresponding {@link ResultAction} type.
 *
 * @author Ulli Hafner
 */
public class BuildHistory {
    /** The build to start the history from. */
    private final Run<?, ?> baseline;
    /** Type of the action that contains the build results. */
    private final Class<? extends ResultAction<? extends BuildResult>> type;
    /** Determines whether only stable builds should be used as reference builds or not. */
    private final boolean useStableBuildAsReference;
    /**
     * Determines if the previous build should always be used as the reference build.
     * @since 1.66
     */
    private final boolean usePreviousBuildAsReference;

    /**
     * Creates a new instance of {@link BuildHistory}.
     *
     * @param baseline
     *            the build to start the history from
     * @param type
     *            type of the action that contains the build results
     * @param usePreviousBuildAsReference
     *            determines whether the previous build should always be used
     *            as the reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @since 1.66
     */
    public BuildHistory(final Run<?, ?> baseline,
            final Class<? extends ResultAction<? extends BuildResult>> type,
            final boolean usePreviousBuildAsReference,
            final boolean useStableBuildAsReference) {
        this.baseline = baseline;
        this.type = type;
        this.usePreviousBuildAsReference = usePreviousBuildAsReference;
        this.useStableBuildAsReference = useStableBuildAsReference;
    }

    /**
     * Determines whether only stable builds should be used as reference builds
     * or not.
     *
     * @return <code>true</code> if only stable builds should be used
     */
    public boolean useOnlyStableBuildsAsReference() {
        return useStableBuildAsReference;
    }

    /**
     * Determines whether to always use the previous build as the reference.
     *
     * @return <code>true</code> if the previous build should always be used.
     */
    public boolean usePreviousBuildAsStable() {
        return usePreviousBuildAsReference;
    }

    /**
     * Returns the time of the baseline build.
     *
     * @return the time
     */
    public Calendar getTimestamp() {
        return baseline.getTimestamp();
    }
    /**
     * Returns whether a reference build result exists.
     *
     * @return <code>true</code> if a reference build result exists.
     */
    private boolean hasReferenceResult() {
        return getReferenceAction() != null;
    }

    /**
     * Returns the annotations of the reference build.
     *
     * @return the annotations of the reference build
     */
    public AnnotationContainer getReferenceAnnotations() {
        ResultAction<? extends BuildResult> action = getReferenceAction();
        if (action != null) {
            return action.getResult().getContainer();
        }
        return new DefaultAnnotationContainer();
    }

    /**
     * Returns the action of the reference build.
     *
     * @return the action of the reference build, or <code>null</code> if no
     *         such build exists
     */
    private ResultAction<? extends BuildResult> getReferenceAction() {
        if (usePreviousBuildAsReference) {
            return getPreviousAction();
        }
        ResultAction<? extends BuildResult> action = getAction(true, useStableBuildAsReference);
        if (action == null) {
            return getPreviousAction(); // fallback, use action of previous build regardless of result
        }
        else {
            return action;
        }
    }

    private ResultAction<? extends BuildResult> getAction(final boolean isStatusRelevant) {
        return getAction(isStatusRelevant, false);
    }

    private ResultAction<? extends BuildResult> getAction(final boolean isStatusRelevant, final boolean mustBeStable) {
        for (Run<?, ?> build = baseline.getPreviousBuild(); build != null; build = build.getPreviousBuild()) {
            ResultAction<? extends BuildResult> action = getResultAction(build);
            if (hasValidResult(build, mustBeStable, action) && isSuccessfulAction(action, isStatusRelevant)) {
                return action;
            }
        }
        return null;
    }

    private boolean isSuccessfulAction(final ResultAction<? extends BuildResult> action, final boolean isStatusRelevant) {
        return action != null && (action.isSuccessful() || !isStatusRelevant);
    }

    /**
     * Returns the result action of the specified build that should be used to
     * compute the history.
     *
     * @param build
     *            the build
     * @return the result action
     */
    @CheckForNull
    public ResultAction<? extends BuildResult> getResultAction(final Run<?, ?> build) {
        return build.getAction(type);
    }

    /**
     * Returns the action of the previous build.
     *
     * @return the action of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @CheckForNull
    private ResultAction<? extends BuildResult> getPreviousAction() {
        return getAction(false);
    }

    /**
     * Returns the reference build or <code>null</code> if there is no such
     * build.
     *
     * @return the reference build
     * @since 1.20
     * @see #hasReferenceBuild()
     */
    @CheckForNull
    public Run<?, ?> getReferenceBuild() {
        ResultAction<? extends BuildResult> action = getReferenceAction();
        if (action != null) {
            Run<?, ?> build = action.getBuild();
            if (hasValidResult(build)) {
                return build;
            }
        }
        return null;
    }

    private boolean hasValidResult(final Run<?, ?> build) {
        return hasValidResult(build, false, null);
    }

    private boolean hasValidResult(final Run<?, ?> build, final boolean mustBeStable, @CheckForNull final ResultAction<? extends BuildResult> action) {
        Result result = build.getResult();

        if (result == null) {
            return false;
        }
        if (mustBeStable) {
            return result == Result.SUCCESS;
        }
        return result.isBetterThan(Result.FAILURE) || isPluginCauseForFailure(action);
    }

    private boolean isPluginCauseForFailure(@CheckForNull final ResultAction<? extends BuildResult> action) {
        if (action == null) {
            return false;
        }
        else {
            return action.getResult().getPluginResult().isWorseOrEqualTo(Result.FAILURE);
        }
    }

    /**
     * Returns whether a reference build is available to compare the results
     * with.
     *
     * @return <code>true</code> if a reference build exists, <code>false</code>
     *         otherwise
     * @since 1.20
     */
    public boolean hasReferenceBuild() {
        return getReferenceBuild() != null;
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     * @see #isEmpty()
     */
    public boolean hasPreviousResult() {
        return getPreviousAction() != null;
    }

    /**
     * Returns whether there is no history available, i.e. the current build is
     * the first valid one.
     *
     * @return <code>true</code> if there is no previous build available
     * @see #hasPreviousResult()
     */
    public boolean isEmpty() {
        return !hasPreviousResult();
    }

    /**
     * Returns the baseline action.
     *
     * @return the baseline action
     * @see #hasPreviousResult()
     * @throws NoSuchElementException
     *             if there is no previous result
     */
    public ResultAction<? extends BuildResult> getBaseline() {
        return getResultAction(baseline);
    }

    /**
     * Returns the previous build result.
     *
     * @return the previous build result
     * @see #hasPreviousResult()
     * @throws NoSuchElementException
     *             if there is no previous result
     */
    public BuildResult getPreviousResult() {
        ResultAction<? extends BuildResult> action = getPreviousAction();
        if (action != null) {
            return action.getResult();
        }
        throw new NoSuchElementException("No previous result available");
    }

    /**
     * Returns the new warnings as a difference between the specified collection
     * of warnings and the warnings of the reference build.
     *
     * @param annotations
     *            the warnings in the current build
     * @return the difference "current build" - "reference build"
     */
    public Collection<FileAnnotation> getNewWarnings(final Set<FileAnnotation> annotations) {
        if (hasReferenceResult()) {
            return AnnotationDifferencer.getNewAnnotations(annotations, getReferenceAnnotations().getAnnotations());
        }
        else {
            return annotations;
        }
    }

    /**
     * Returns the fixed warnings as a difference between the warnings of the
     * reference build and the specified collection of warnings.
     *
     * @param annotations
     *            the warnings in the current build
     * @return the difference "reference build" - "current build"
     */
    public Collection<FileAnnotation> getFixedWarnings(final Set<FileAnnotation> annotations) {
        if (hasReferenceResult()) {
            return AnnotationDifferencer.getFixedAnnotations(annotations, getReferenceAnnotations().getAnnotations());
        }
        else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the health descriptor used for the builds.
     *
     * @return the health descriptor
     */
    public AbstractHealthDescriptor getHealthDescriptor() {
        return getBaseline().getHealthDescriptor();
    }

    /**
     * Creates a new instance of {@link BuildHistory}.
     *
     * @param baseline
     *            the build to start the history from
     * @param type
     *            type of the action that contains the build results
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @since 1.47
     * @deprecated use {@link #BuildHistory(AbstractBuild, Class, boolean, boolean)}
     */
    @Deprecated
    public BuildHistory(final Run<?, ?> baseline, final Class<? extends ResultAction<? extends BuildResult>> type,
            final boolean useStableBuildAsReference) {
        this(baseline, type, false, useStableBuildAsReference);
    }

    /**
     * Creates a new instance of {@link BuildHistory}.
     *
     * @param baseline
     *            the build to start the history from
     * @param type
     *            type of the action that contains the build results
     * @deprecated use {@link #BuildHistory(AbstractBuild, Class, boolean, boolean)}
     */
    @Deprecated
    public BuildHistory(final AbstractBuild<?, ?> baseline, final Class<? extends ResultAction<? extends BuildResult>> type) {
        this(baseline, type, false);
    }
}

