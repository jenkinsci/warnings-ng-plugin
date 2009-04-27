package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.plugins.warnings.util.BuildResult;
import hudson.plugins.warnings.util.FilesParser;
import hudson.plugins.warnings.util.HealthAwarePublisher;
import hudson.plugins.warnings.util.ParserResult;
import hudson.plugins.warnings.util.PluginLogger;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class WarningsPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -5936973521277401764L;
    /** Descriptor of this publisher. */
    public static final WarningsDescriptor WARNINGS_DESCRIPTOR = new WarningsDescriptor();
    /** Ant file-set pattern of files to work with. */
    private final String pattern;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;
    /** Name of parsers to use for scanning the logs. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private Set<String> parserNames = new HashSet<String>();
    /** Determines whether the plug-in should run for failed builds, too. */
    private final boolean canRunOnFailed;

    /**
     * Creates a new instance of <code>WarningPublisher</code>.
     *
     * @param threshold
     *            Annotation threshold to be reached if a build should be considered as
     *            unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be considered as
     *            failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of annotations is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of annotations is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param pattern
     *            Ant file-set pattern that defines the files to scan for
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public WarningsPublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit,
            final String pattern, final String excludePattern, final String defaultEncoding,
            final boolean canRunOnFailed) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, defaultEncoding, "WARNINGS");
        this.pattern = pattern;
        this.canRunOnFailed = canRunOnFailed;
        this.excludePattern = StringUtils.stripToNull(excludePattern);
    }
    // CHECKSTYLE:ON

    /**
     * Returns the names of the configured parsers of this publisher.
     *
     * @return the parser names
     */
    public Set<String> getParserNames() {
        return parserNames;
    }

    /**
     * Returns whether this plug-in can run for failed builds, too.
     *
     * @return the can run on failed
     */
    public boolean getCanRunOnFailed() {
        return canRunOnFailed;
    }

    /**
     * Adds the specified parsers to this publisher.
     *
     * @param parserNames
     *            the parsers to use when scanning the files
     */
    public void setParserNames(final Set<String> parserNames) {
        this.parserNames = parserNames;
    }

    /**
     * Creates a new parser set for old versions of this class.
     *
     * @return this
     */
    @Override
    protected Object readResolve() {
        super.readResolve();
        if (parserNames == null) {
            parserNames = new HashSet<String>();
        }
        return this;
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
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new WarningsProjectAction(project, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Parsing warnings in log file...");
        File logFile = build.getLogFile();

        ParserResult project;
        if (StringUtils.isNotBlank(getPattern())) {
            FilesParser parser = new FilesParser(logger, getPattern(), new FileWarningsParser(parserNames, getDefaultEncoding(), getExcludePattern()), isMavenBuild(build), isAntBuild(build));
            project = build.getProject().getWorkspace().act(parser);
        }
        else {
            project = new ParserResult(build.getProject().getWorkspace());
        }

        project.addAnnotations(new ParserRegistry(ParserRegistry.getParsers(parserNames), getDefaultEncoding(), getExcludePattern()).parse(logFile));
        project = build.getProject().getWorkspace().act(new AnnotationsClassifier(project, getDefaultEncoding()));

        WarningsResult result = new WarningsResultBuilder().build(build, project, getDefaultEncoding());
        build.getActions().add(new WarningsResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor<Publisher> getDescriptor() {
        return WARNINGS_DESCRIPTOR;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean canContinue(final Result result) {
        if (canRunOnFailed) {
            return true;
        }
        else {
            return super.canContinue(result);
        }
    }
}
