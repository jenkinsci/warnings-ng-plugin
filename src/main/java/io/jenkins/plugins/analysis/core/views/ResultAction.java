package io.jenkins.plugins.analysis.core.views;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Summary;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.quality.QualityGate;

import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Controls the live cycle of the results in a job. This action persists the results of a build and displays them on the
 * build page. The actual visualization of the results is defined in the matching {@code summary.jelly} file. This
 * action also provides access to the static analysis details: these are rendered using a new {@link IssuesDetail}
 * instance.
 *
 * @author Ulli Hafner
 */
public class ResultAction implements HealthReportingAction, LastBuildAction, RunAction2, StaplerProxy {
    private transient Run<?, ?> owner;

    private final AnalysisResult result;
    private final HealthDescriptor healthDescriptor;
    private final String id;
    private final String name;
    private final Charset charset;

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
        this.owner = owner;
        this.result = result;
        this.healthDescriptor = healthDescriptor;
        this.id = id;
        this.name = name;
        this.charset = charset;
    }

    /**
     * Returns the ID (and URL) of this action.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the associated build/run that created the static analysis result.
     *
     * @return the run
     */
    public Run<?, ?> getOwner() {
        return owner;
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

    @Override
    public String getDisplayName() {
        return getLabelProvider().getLinkName();
    }

    @Override
    public String getUrlName() {
        return getLabelProvider().getResultUrl();
    }

    @Override
    public HealthReport getBuildHealth() {
        return new HealthReportBuilder(healthDescriptor).computeHealth(getResult());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new JobAction(owner.getParent(), getLabelProvider(), healthDescriptor));
    }

    public AnalysisResult getResult() {
        return result;
    }

    @Override
    public String getIconFileName() {
        if (getResult().getTotalSize() > 0) {
            return getSmallImage();
        }
        return null;
    }

    /**
     * Returns whether a large image is defined.
     *
     * @return {@code true} if a large image is defined, {@code false} otherwise
     */
    public boolean hasLargeImage() {
        return StringUtils.isNotBlank(getLargeImageName());
    }

    /**
     * Returns the URL of the 48x48 image used in the build summary.
     *
     * @return the URL of the image
     */
    public String getLargeImageName() {
        return getLabelProvider().getLargeIconUrl();
    }

    /**
     * Returns the URL of the 24x24 image used in the build link.
     *
     * @return the URL of the image
     */
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
     * Returns a summary message of the static analysis run. This message is shown in the 'summary.jelly' view.
     *
     * @return summary message (HTML)
     */
    public String getSummary() {
        return new Summary(getLabelProvider(), getResult()).create();
    }

    /**
     * Returns whether the static analysis result is considered successfully with respect to the used {@link
     * QualityGate}.
     *
     * @return {@code true} if the result is successful, {@code false} if the result has been set to {@link
     *         Result#UNSTABLE} or {@link Result#FAILURE}.
     */
    public boolean isSuccessful() {
        return getResult().isSuccessful();
    }

    @Override
    public String toString() {
        return String.format("%s for %s", getClass().getName(), getLabelProvider().getName());
    }

    private StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProviderFactory().create(id, name);
    }

    /**
     * Returns the detail view for issues for all Stapler requests.
     *
     * @return the detail view for issues
     */
    @Override
    public Object getTarget() {
        return new IssuesDetail(owner, result, getLabelProvider(), charset);
    }
}
