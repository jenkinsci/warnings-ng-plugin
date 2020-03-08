package io.jenkins.plugins.analysis.core.model;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.TrendChartType;

/**
 * Controls the live cycle of the analysis results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code
 * summary.jelly} file. This action also provides access to the static analysis details: these are rendered using a new
 * {@link IssuesDetail} instance.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings(value = "SE", justification = "transient field owner ist restored using a Jenkins callback")
public class ResultAction implements HealthReportingAction, LastBuildAction, RunAction2, StaplerProxy, Serializable {
    private static final long serialVersionUID = 6683647181785654908L;

    private transient Run<?, ?> owner;

    private final AnalysisResult result;
    private final HealthDescriptor healthDescriptor;
    private final String id;
    private final String name;
    private final String charset;
    private TrendChartType trendChartType;

    /**
     * Creates a new instance of {@link ResultAction}.
     *
     * @param owner
     *         the associated build/run that created the static analysis result
     * @param result
     *         the result of the static analysis run
     * @param healthDescriptor
     *         the health descriptor of the static analysis run
     * @param id
     *         the ID of the results
     * @param name
     *         the optional name of the results
     * @param charset
     *         the charset to use to display source files
     */
    public ResultAction(final Run<?, ?> owner, final AnalysisResult result, final HealthDescriptor healthDescriptor,
            final String id, final String name, final Charset charset) {
        this(owner, result, healthDescriptor, id, name, charset, TrendChartType.AGGREGATION_TOOLS);
    }

    /**
     * Creates a new instance of {@link ResultAction}.
     *
     * @param owner
     *         the associated build/run that created the static analysis result
     * @param result
     *         the result of the static analysis run
     * @param healthDescriptor
     *         the health descriptor of the static analysis run
     * @param id
     *         the ID of the results
     * @param name
     *         the optional name of the results
     * @param charset
     *         the charset to use to display source files
     * @param trendChartType
     *         determines if the trend chart will be shown
     */
    public ResultAction(final Run<?, ?> owner, final AnalysisResult result, final HealthDescriptor healthDescriptor,
            final String id, final String name, final Charset charset, final TrendChartType trendChartType) {
        this.owner = owner;
        this.result = result;
        this.healthDescriptor = healthDescriptor;
        this.id = id;
        this.name = name;
        this.charset = charset.name();
        this.trendChartType = trendChartType;
    }

    /**
     * Called after de-serialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        if (trendChartType == null) {
            trendChartType = TrendChartType.TOOLS_ONLY;
        }
        return this;
    }

    /**
     * Returns the ID (and URL) of this action.
     *
     * @return the ID
     */
    @Whitelisted
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the static analysis tool.
     *
     * @return the ID
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the associated build/run that created the static analysis result.
     *
     * @return the run
     */
    public Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the build history for this action.
     *
     * @return the history
     */
    public History createBuildHistory() {
        return new AnalysisHistory(owner, new ByIdResultSelector(getId()));
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
        result.setOwner(r);
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        onAttached(r);
    }

    @Whitelisted
    @Override
    public String getDisplayName() {
        return getLabelProvider().getLinkName();
    }

    @Override
    public String getUrlName() {
        return getLabelProvider().getId();
    }

    /**
     * Returns the URL of this action, relative to the context root of Jenkins.
     *
     * @return the relative URL, like job/foo/32/analysis/
     */
    public String getRelativeUrl() {
        return getOwner().getUrl() + getUrlName();
    }

    /**
     * Gets the absolute path to the build from the owner. This is needed for testing due to {@link
     * Run#getAbsoluteUrl()} being final and therefore not mockable.
     *
     * @return the absolute url to the job
     */
    @SuppressWarnings("deprecation") // this is the only way for remote API calls to obtain the absolute path
    public String getAbsoluteUrl() {
        return getOwner().getAbsoluteUrl();
    }

    @Override
    @Nullable
    public HealthReport getBuildHealth() {
        return new HealthReportBuilder().computeHealth(healthDescriptor, getLabelProvider(),
                getResult().getSizePerSeverity());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(
                new JobAction(owner.getParent(), getLabelProvider(), result.getSizePerOrigin().size(),
                        trendChartType));
    }

    @Whitelisted
    public AnalysisResult getResult() {
        return result;
    }

    @Override
    public String getIconFileName() {
        return getSmallImage();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResultAction that = (ResultAction) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns whether a large image is defined.
     *
     * @return {@code true} if a large image is defined, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean hasLargeImage() {
        return StringUtils.isNotBlank(getLargeImageName());
    }

    /**
     * Returns the URL of the 48x48 image used in the build summary.
     *
     * @return the URL of the image
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // Called by jelly view
    public String getLargeImageName() {
        return getLabelProvider().getLargeIconUrl();
    }

    /**
     * Returns the URL of the 24x24 image used in the build link.
     *
     * @return the URL of the image
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getSmallImageName() {
        return getSmallImage();
    }

    /**
     * Returns the URL of the 24x24 image used in the build link.
     *
     * @return the URL of the image
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // Called by jelly view
    public String getSmallImage() {
        return getLabelProvider().getSmallIconUrl();
    }

    /**
     * Returns a summary message of the static analysis run. This message is shown in the 'summary.jelly' view.
     *
     * @return summary message (HTML)
     */
    public String getSummary() {
        return new Summary(getLabelProvider(), getResult()).create();
    }

    /**
     * Returns whether the static analysis result is considered successfully with respect to the used {@link
     * QualityGateEvaluator}.
     *
     * @return {@code true} if the result is successful, {@code false} if the result has been set to {@link
     *         Result#UNSTABLE} or {@link Result#FAILURE}.
     */
    @Whitelisted
    public boolean isSuccessful() {
        return getResult().isSuccessful();
    }

    @Override
    public String toString() {
        return String.format("%s for %s", getClass().getName(), getLabelProvider().getName());
    }

    /**
     * Returns the {@link StaticAnalysisLabelProvider} for this action.
     *
     * @return the label provider for this tool
     */
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProviderFactory().create(id, name);
    }

    /**
     * Returns the detail view for issues for all Stapler requests.
     *
     * @return the detail view for issues
     */
    @Override
    public Object getTarget() {
        return new IssuesDetail(owner, result, getLabelProvider(), healthDescriptor, Charset.forName(charset));
    }

    /**
     * Empty method as workaround for Stapler bug: JavaScript method in target object is not found.
     *
     * @return unused string (since Firefox requires that Ajax calls return something)
     */
    @JavaScriptMethod
    @SuppressWarnings("unused")
    public String resetReference() {
        // Empty method as workaround for Stapler bug that does not find JavaScript proxy methods in target object IssueDetail
        return "{}";
    }
}
