package io.jenkins.plugins.analysis.core.steps;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;
import io.jenkins.plugins.analysis.core.quality.HealthReportBuilder;
import io.jenkins.plugins.analysis.core.views.IssuesDetail;
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
public class ResultAction implements HealthReportingAction, LastBuildAction, RunAction2, StaplerProxy {
    private transient Run<?, ?> run;

    private final AnalysisResult result;
    private final String id;
    private final HealthDescriptor healthDescriptor;
    private final String name;

    /**
     * Creates a new instance of {@link ResultAction}.
     *
     * @param run
     *         the associated build of this action
     * @param result
     *         the result of the static analysis run
     * @param healthDescriptor
     *         defines the health for the current result
     * @param id
     *         the ID of the static analysis tool
     * @param name
     *         The name of the tool. If empty the name is resolved using the ID.
     */
    public ResultAction(final Run<?, ?> run, final AnalysisResult result, final HealthDescriptor healthDescriptor,
            final String id, final String name) {
        this.run = run;
        this.result = result;
        this.id = id;
        this.healthDescriptor = healthDescriptor;
        this.name = name;
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
        return getLabelProvider().getLinkName();
    }

    @Override
    public String getUrlName() {
        return getLabelProvider().getResultUrl();
    }

    @Override
    @Exported
    public HealthReport getBuildHealth() {
        return new HealthReportBuilder(healthDescriptor).computeHealth(getResult());
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new JobAction(run.getParent(), id, name));
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

    public Issues getIssues() {
        return getResult().getProject();
    }

    public Issues getNewIssues() {
        return getResult().getNewWarnings();
    }

    public Issues getFixedIssues() {
        return getResult().getFixedWarnings();
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
        return getLabelProvider().getLargeIconUrl();
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
        return getLabelProvider().getSmallIconUrl();
    }

    @Exported
    public boolean isSuccessful() {
        return getResult().isSuccessful();
    }

//    /**
//     * Returns a new view, that shows the selected issues.
//     *
//     * @param link
//     *            the link to identify the sub page to show
//     * @param request
//     *            Stapler request
//     * @param response
//     *            Stapler response
//     * @return the dynamic result of the analysis (detail page).
//     */
//    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
//        try {
//            return new DetailFactory().createTrendDetails(link, run, getIssues(), getFixedIssues(), getNewIssues(),
//                    getResult().getErrors(), getResult().getDefaultEncoding(), this);
//        }
//        catch (NoSuchElementException ignored) {
//            redirectToProject(response);
//
//            return this; // fallback on broken URLs
//        }
//    }
//
//    private void redirectToProject(final StaplerResponse response) {
//        try {
//            response.sendRedirect2("../");
//        }
//        catch (IOException ignored) {
//            // ignore
//        }
//    }
//
    @Override
    public String toString() {
        return String.format("%s for %s", getClass().getName(), getLabelProvider().getName());
    }

    private StaticAnalysisLabelProvider getLabelProvider() {
        return StaticAnalysisTool.find(id, name);
    }

    @Override
    public Object getTarget() {
        AnalysisResult result = getResult();

        return new IssuesDetail(run, result.getProject(), result.getFixedWarnings(), result.getNewWarnings(),
                result.getDefaultEncoding(), Messages._Default_Name());
    }
}
