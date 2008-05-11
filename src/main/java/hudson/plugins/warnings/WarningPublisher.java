package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.plugins.warnings.util.HealthAwarePublisher;
import hudson.plugins.warnings.util.HealthReportBuilder;
import hudson.plugins.warnings.util.model.JavaProject;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class WarningPublisher extends HealthAwarePublisher {
    /** Descriptor of this publisher. */
    public static final WarningDescriptor WARNINGS_DESCRIPTOR = new WarningDescriptor();

    /**
     * Creates a new instance of <code>WarningPublisher</code>.
     *
     * @param threshold
     *            Annotation threshold to be reached if a build should be considered as
     *            unstable.
     * @param healthy
     *            Report health as 100% when the number of annotations is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of annotations is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @stapler-constructor
     */
    public WarningPublisher(final String threshold, final String healthy, final String unHealthy, final String height) {
        super(threshold, healthy, unHealthy, height, "WARNINGS");
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new WarningsProjectAction(project, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public JavaProject perform(final AbstractBuild<?, ?> build, final PrintStream logger) throws InterruptedException, IOException {
        log(logger, "Parsing warnings in log file...");
        File logFile = build.getLogFile();

        JavaProject project = new JavaProject();
        project.addAnnotations(new ParserRegistry().parse(logFile));

        WarningsResult result = new WarningsResultBuilder().build(build, project);
        HealthReportBuilder healthReportBuilder = createHealthReporter(
                Messages.Warnings_ResultAction_HealthReportSingleItem(),
                Messages.Warnings_ResultAction_HealthReportMultipleItem("%d"));
        build.getActions().add(new WarningsResultAction(build, healthReportBuilder, result));

        return project;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return WARNINGS_DESCRIPTOR;
    }
}
