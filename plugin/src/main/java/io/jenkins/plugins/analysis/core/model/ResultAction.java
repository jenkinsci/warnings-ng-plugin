package io.jenkins.plugins.analysis.core.model;

import jenkins.management.Badge;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.Generated;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jvnet.localizer.Localizable;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGateResult;

/**
 * Controls the life cycle of the analysis results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file. This action also provides access to the static analysis details: these are rendered using a new {@link
 * IssuesDetail} instance.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("ClassFanOutComplexity")
@SuppressFBWarnings(value = "SE", justification = "transient field owner ist restored using a Jenkins callback")
public class ResultAction implements HealthReportingAction, LastBuildAction, RunAction2, StaplerProxy, Serializable {
    @Serial
    private static final long serialVersionUID = 6683647181785654908L;

    private transient Run<?, ?> owner;

    private final AnalysisResult result;
    private final HealthDescriptor healthDescriptor;
    private final String id;
    private final String name;
    private /* almost final */ String icon;
    private final String charset;
    private /* almost final */ TrendChartType trendChartType;

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
     * @param icon
     *         the optional icon of the results
     * @param charset
     *         the charset to use to display source files
     * @param trendChartType
     *         determines if the trend chart will be shown
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public ResultAction(final Run<?, ?> owner, final AnalysisResult result, final HealthDescriptor healthDescriptor,
            final String id, final String name, final String icon,
            final Charset charset, final TrendChartType trendChartType) {
        this.owner = owner;
        this.result = result;
        this.healthDescriptor = healthDescriptor;
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.charset = charset.name();
        this.trendChartType = trendChartType;
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    @Serial
    protected Object readResolve() {
        if (trendChartType == null) {
            trendChartType = TrendChartType.TOOLS_ONLY;
        }
        if (icon == null) {
            icon = StringUtils.EMPTY;
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

    @Whitelisted
    public QualityGateResult getQualityGateResult() {
        return getResult().getQualityGateResult();
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
        return StringUtils.defaultIfEmpty(id, getLabelProvider().getId());
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
        return new JenkinsFacade().getAbsoluteUrl(StringUtils.removeEnd(getOwner().getUrl(), "/"), getUrlName());
    }

    @Override
    @CheckForNull
    public HealthReport getBuildHealth() {
        return new HealthReportBuilder().computeHealth(healthDescriptor, getLabelProvider(),
                getResult().getSizePerSeverity());
    }

    HealthDescriptor getHealthDescriptor() {
        return healthDescriptor;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Set.of(new JobAction(owner.getParent(), getLabelProvider(), result.getSizePerOrigin().size(),
                trendChartType, getUrlName()));
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

        var that = (ResultAction) o;

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
    public String getSmallImage() {
        return getLabelProvider().getSmallIconUrl();
    }

    /**
     * Returns the model for the summary of the static analysis run. This model is used as input in the 'summary.jelly'
     * view.
     *
     * @return model to build the summary message on the client side
     */
    @SuppressWarnings("unused") // Called by jelly view
    public SummaryModel getSummaryModel() {
        return new SummaryModel(getLabelProvider(), getResult());
    }

    /**
     * Returns whether the static analysis result is considered successfully with respect to the evaluated quality gates.
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
        return "%s for %s".formatted(getClass().getName(), getLabelProvider().getName());
    }

    /**
     * Returns the {@link StaticAnalysisLabelProvider} for this action.
     *
     * @return the label provider for this tool
     */
    public StaticAnalysisLabelProvider getLabelProvider() {
        var registeredLabelProvider = new LabelProviderFactory().create(getParserId(), name);
        if (StringUtils.isBlank(icon)) {
            return registeredLabelProvider;
        }

        return new CustomIconLabelProvider(registeredLabelProvider, icon);
    }

    private String getParserId() {
        var parserId = getResult().getParserId();
        if (isValidId(parserId)) {
            return parserId;
        }
        return id;
    }

    private boolean isValidId(final String parserId) {
        return StringUtils.isNotBlank(parserId) && !"-".equals(parserId);
    }

    /**
     * Returns the detail view for issues for all Stapler requests.
     *
     * @return the detail view for issues
     */
    @Override
    public IssuesDetail getTarget() {
        return new IssuesDetail(owner, result, getLabelProvider(), healthDescriptor, Charset.forName(charset));
    }

    /**
     * Empty method as workaround for Stapler bug: JavaScript method in the target object is not found.
     *
     * @return unused string (since Firefox requires that Ajax calls return something)
     */
    @JavaScriptMethod
    @SuppressWarnings("unused")
    public String resetReference() {
        // Empty method as workaround for Stapler bug that does not find JavaScript proxy methods in target object IssueDetail
        return "{}";
    }

    /**
     * Displays a badge if there are issues.
     *
     * <p>
     * Only for use in Jelly.
     *
     * @return the badge or {@code null} if there are no issues
     */
    @Restricted(DoNotUse.class)
    public Badge getBadge() {
        var warningActionsCount = getResult().getTotalSize();

        if (warningActionsCount == 0) {
            return null;
        }

        return new Badge(String.valueOf(warningActionsCount), Messages.ResultAction_Badge(warningActionsCount), Badge.Severity.WARNING);
    }

    private static class CustomIconLabelProvider extends StaticAnalysisLabelProvider {
        @Override
        public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url, final Report report) {
            return decorated.getIssuesModel(build, url, report);
        }

        @Override
        public DefaultAgeBuilder getAgeBuilder(final Run<?, ?> owner, final String url) {
            return decorated.getAgeBuilder(owner, url);
        }

        @Override
        public FileNameRenderer getFileNameRenderer(final Run<?, ?> owner) {
            return decorated.getFileNameRenderer(owner);
        }

        @VisibleForTesting
        @Override
        public String getDefaultName() {
            return decorated.getDefaultName();
        }

        @Override
        public String getId() {
            return decorated.getId();
        }

        @Override
        public String getName() {
            return decorated.getName();
        }

        @Override
        public StaticAnalysisLabelProvider setName(@CheckForNull final String name) {
            return decorated.setName(name);
        }

        @Override
        @Generated
        public String toString() {
            return decorated.toString();
        }

        @Override
        public String getLinkName() {
            return decorated.getLinkName();
        }

        @Override
        public String getTrendName() {
            return decorated.getTrendName();
        }

        @Override
        public String getToolTip(final int numberOfItems) {
            return decorated.getToolTip(numberOfItems);
        }

        @Override
        public Localizable getToolTipLocalizable(final int numberOfItems) {
            return decorated.getToolTipLocalizable(numberOfItems);
        }

        @Override
        public String getDescription(final Issue issue) {
            return decorated.getDescription(issue);
        }

        @Override
        public String getSourceCodeDescription(final Run<?, ?> build, final Issue issue) {
            return decorated.getSourceCodeDescription(build, issue);
        }

        private final StaticAnalysisLabelProvider decorated;
        private final String icon;

        CustomIconLabelProvider(final StaticAnalysisLabelProvider decorated, final String icon) {
            super(decorated.getId(), decorated.getName());
            this.decorated = decorated;

            this.icon = icon;
        }

        @Override
        public String getSmallIconUrl() {
            return icon;
        }

        @Override
        public String getLargeIconUrl() {
            return icon;
        }
    }
}
