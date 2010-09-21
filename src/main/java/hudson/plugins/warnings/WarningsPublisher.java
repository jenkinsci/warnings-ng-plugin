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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.Sets;

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
     * @param healthy
     *            Report health as 100% when the number of annotations is less
     *            than this value
     * @param unHealthy
     *            Report health as 0% when the number of annotations is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     * @param unstableTotalAll
     *            annotation threshold
     * @param unstableTotalHigh
     *            annotation threshold
     * @param unstableTotalNormal
     *            annotation threshold
     * @param unstableTotalLow
     *            annotation threshold
     * @param unstableNewAll
     *            annotation threshold
     * @param unstableNewHigh
     *            annotation threshold
     * @param unstableNewNormal
     *            annotation threshold
     * @param unstableNewLow
     *            annotation threshold
     * @param failedTotalAll
     *            annotation threshold
     * @param failedTotalHigh
     *            annotation threshold
     * @param failedTotalNormal
     *            annotation threshold
     * @param failedTotalLow
     *            annotation threshold
     * @param failedNewAll
     *            annotation threshold
     * @param failedNewHigh
     *            annotation threshold
     * @param failedNewNormal
     *            annotation threshold
     * @param failedNewLow
     *            annotation threshold
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param canScanConsole
     *            Determines whether the console should be scanned.
     * @param pattern
     *            Ant file-set pattern that defines the files to scan for
     * @param includePattern
     *            Ant file-set pattern of files to include in report
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public WarningsPublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed,
            final boolean canScanConsole, final String pattern, final String includePattern, final String excludePattern) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, "WARNINGS");
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
    public List<String> getParserNames() {
        return ParserRegistry.filterExistingParserNames(parserNames);
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

        Set<String> validParsers = Sets.newHashSet(getParserNames());
        ParserResult project;
        if (StringUtils.isNotBlank(getPattern())) {
            logger.log("Parsing warnings in files: " + getPattern());
            FilesParser parser = new FilesParser(logger, getPattern(),
                    new FileWarningsParser(validParsers, getDefaultEncoding(), getIncludePattern(), getExcludePattern()), isMavenBuild(build), isAntBuild(build));
            project = build.getWorkspace().act(parser);
        }
        else {
            project = new ParserResult(build.getWorkspace());
        }

        if (!ignoreConsole || StringUtils.isBlank(getPattern())) {
            logger.log("Parsing warnings in console log...");
            ParserRegistry registry = new ParserRegistry(ParserRegistry.getParsers(validParsers),
                    getDefaultEncoding(), getIncludePattern(), getExcludePattern());
            Collection<FileAnnotation> warnings = registry.parse(logFile);
            if (!build.getWorkspace().isRemote()) {
                String workspace = build.getWorkspace().getRemote();
                ModuleDetector detector = new ModuleDetector(new File(workspace));
                for (FileAnnotation annotation : warnings) {
                    String module = detector.guessModuleName(annotation.getFileName());
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
