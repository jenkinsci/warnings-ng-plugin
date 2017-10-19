package io.jenkins.plugins.analysis.core.steps;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Run;

/**
 * Controls the live cycle of the results in a job. This action persists the results of a build and displays them on the
 * build page. The actual visualization of the results is defined in the matching {@code summary.jelly} file. <p>
 * Moreover, this class renders the results trend. </p>
 *
 * @author Ulli Hafner
 */
//CHECKSTYLE:COUPLING-OFF
@ExportedBean
public class ResultAction implements StaplerProxy, HealthReportingAction, LastBuildAction, RunAction2 {
    private transient Run<?, ?> run;

    private final AnalysisResult result;
    private final String id;
    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param run
     *         the associated build of this action
     * @param id
     *         the ID of the parser
     * @param result
     *         the result of the static analysis run
     * @param healthDescriptor
     *         defines the health for the current result
     */
    public ResultAction(final Run<?, ?> run, final String id, final AnalysisResult result,
            final HealthDescriptor healthDescriptor) {
        this.run = run;
        this.result = result;
        this.id = id;
        this.healthDescriptor = healthDescriptor;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        run = r;
        result.setRun(r);
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        onAttached(r);
    }

    public String getId() {
        return id;
    }

    @Override
    @Exported
    public String getDisplayName() {
        return getIssueParser().getLinkName();
    }

    @Override
    public String getUrlName() {
        return getIssueParser().getResultUrl();
    }

    @Override
    @Exported
    public HealthReport getBuildHealth() {
        return new HealthReportBuilder(healthDescriptor).computeHealth(getResult());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new JobAction(run.getParent(), id));
    }

    @Override
    public final Object getTarget() {
        return getResult();
    }

    @Exported
    public AnalysisResult getResult() {
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
     * @return <code>true</code> if a large image is defined, <code>false</code> otherwise. If no large image is
     *         defined, then the attribute {@code icon} must to be provided in jelly tag {@code summary}.
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

    @Exported
    public boolean isSuccessful() {
        return getResult().isSuccessful();
    }

    private StaticAnalysisLabelProvider getIssueParser() {
        return StaticAnalysisTool.find(id);
    }
}
