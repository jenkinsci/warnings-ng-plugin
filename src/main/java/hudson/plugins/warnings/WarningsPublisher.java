package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.plugins.warnings.util.FilesParser;
import hudson.plugins.warnings.util.HealthAwarePublisher;
import hudson.plugins.warnings.util.ParserResult;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class WarningsPublisher extends HealthAwarePublisher {
    /** Descriptor of this publisher. */
    public static final WarningsDescriptor WARNINGS_DESCRIPTOR = new WarningsDescriptor();
    /** Ant file-set pattern of files to work with. */
    private final String pattern;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;

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
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param pattern
     *            Ant file-set pattern that defines the files to scan for
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     */
    @DataBoundConstructor
    public WarningsPublisher(final String threshold, final String healthy, final String unHealthy, final String height, final String thresholdLimit, final String pattern, final String excludePattern) {
        super(threshold, healthy, unHealthy, height, thresholdLimit, "WARNINGS");
        this.pattern = pattern;
        this.excludePattern = StringUtils.stripToNull(excludePattern);
    }

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the Ant file-set pattern of files to exclude from report.
     *
     * @return Ant file-set pattern of files to exclude from report
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new WarningsProjectAction(project, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public ParserResult perform(final AbstractBuild<?, ?> build, final PrintStream logger) throws InterruptedException, IOException {
        log(logger, "Parsing warnings in log file...");
        File logFile = build.getLogFile();

        ParserResult project;
        if (StringUtils.isNotBlank(getPattern())) {
            FilesParser parser = new FilesParser(logger, getPattern(), new FileWarningsParser(getExcludePattern()), isMavenBuild(build), isAntBuild(build));
            project = build.getProject().getWorkspace().act(parser);
        }
        else {
            project = new ParserResult(build.getProject().getWorkspace());
        }

        project.addAnnotations(new ParserRegistry(getExcludePattern()).parse(logFile));

        WarningsResult result = new WarningsResultBuilder().build(build, project);
        build.getActions().add(new WarningsResultAction(build, this, result));

        return project;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return WARNINGS_DESCRIPTOR;
    }
}
