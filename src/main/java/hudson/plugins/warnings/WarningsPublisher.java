package hudson.plugins.warnings;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AnnotationsClassifier;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.ParserRegistry;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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

    /** Ant file-set pattern of files to work with. */
    private final String pattern;
    /** Ant file-set pattern of files to include to report. */
    private final String includePattern;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;
    /** Name of parsers to use for scanning the logs. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private Set<String> parserNames = new HashSet<String>();
    /** Determines whether the console should be ignored. */
    private final boolean ignoreConsole;

    /**
     * Creates a new instance of <code>WarningPublisher</code>.
     *
     * @param threshold
     *            Annotation threshold to be reached if a build should be
     *            considered as unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be
     *            considered as failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of annotations is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of annotations is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param pattern
     *            Ant file-set pattern that defines the files to scan for
     * @param includePattern
     *            Ant file-set pattern of files to include in report
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param canScanConsole
     *            Determines whether the console should be scanned.
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public WarningsPublisher(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit,
            final String pattern, final String includePattern, final String excludePattern,
            final String defaultEncoding, final boolean useDeltaValues, final boolean canRunOnFailed,
            final boolean canScanConsole) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues, canRunOnFailed, "WARNINGS");
        this.pattern = pattern;
        ignoreConsole = !canScanConsole;
        this.includePattern = StringUtils.stripToNull(includePattern);
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
     * Returns whether this plug-in should scan the console or not.
     *
     * @return the can run on failed
     */
    public boolean getCanScanConsole() {
        return !ignoreConsole;
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
     * Returns the Ant file-set pattern of files to include in report.
     *
     * @return Ant file-set pattern of files to include in report
     */
    public String getIncludePattern() {
        return includePattern;
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
        return new WarningsProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        File logFile = build.getLogFile();

        ParserResult project;
        if (StringUtils.isNotBlank(getPattern())) {
            logger.log("Parsing warnings in files: " + getPattern());
            FilesParser parser = new FilesParser(logger, getPattern(), new FileWarningsParser(parserNames, getDefaultEncoding(), getIncludePattern(), getExcludePattern()), isMavenBuild(build), isAntBuild(build));
            project = build.getWorkspace().act(parser);
        }
        else {
            project = new ParserResult(build.getWorkspace());
        }

        if (!ignoreConsole || StringUtils.isBlank(getPattern())) {
            logger.log("Parsing warnings in console log...");
            ParserRegistry registry = new ParserRegistry(ParserRegistry.getParsers(parserNames),
                    getDefaultEncoding(), getIncludePattern(), getExcludePattern());
            Collection<FileAnnotation> warnings = registry.parse(logFile);
            if (!build.getWorkspace().isRemote()) {
                String workspace = build.getWorkspace().getRemote();
                ModuleDetector detector = new ModuleDetector(new File(workspace));
                String module = detector.guessModuleName(build.getWorkspace().getRemote());
                for (FileAnnotation annotation : warnings) {
                    annotation.setModuleName(module);
                }
            }

            project.addAnnotations(warnings);
        }
        project = build.getWorkspace().act(new AnnotationsClassifier(project, getDefaultEncoding()));
        for (FileAnnotation annotation : project.getAnnotations()) {
            annotation.setPathName(build.getWorkspace().getRemote());
        }

        WarningsResult result = new WarningsResult(build, getDefaultEncoding(), project);
        build.getActions().add(new WarningsResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public WarningsDescriptor getDescriptor() {
        return (WarningsDescriptor)super.getDescriptor();
    }
}
