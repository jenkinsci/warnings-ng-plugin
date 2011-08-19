package hudson.plugins.warnings;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AnnotationsClassifier;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.NullModuleDetector;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.StringPluginLogger;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class WarningsPublisher extends HealthAwarePublisher {
    private static final String PLUGIN_NAME = "WARNINGS";

    private static final long serialVersionUID = -5936973521277401764L;

    /** Ant file-set pattern of files to include to report. */
    private final String includePattern;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;

    /** File pattern and parser configurations. @since 3.19 */
    private List<ParserConfiguration> parserConfigurations = Lists.newArrayList();
    /** Parser to scan the console log. @since 3.19 */
    private Set<String> consoleLogParsers = Sets.newHashSet();

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
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven POM or Ant build files
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
            final boolean canRunOnFailed, final boolean shouldDetectModules,
            final String includePattern, final String excludePattern) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, shouldDetectModules, PLUGIN_NAME);
        this.includePattern = StringUtils.stripToNull(includePattern);
        this.excludePattern = StringUtils.stripToNull(excludePattern);
    }
    // CHECKSTYLE:ON

    /**
     * Returns the names of the configured parsers for the console log.
     *
     * @return the parser names
     */
    public List<String> getConsoleLogParsers() {
        return ParserRegistry.filterExistingParserNames(consoleLogParsers);
    }

    /**
     * Adds the specified parsers to this publisher.
     *
     * @param parserNames
     *            the parsers to use when scanning the console log
     */
    public void setConsoleLogParsers(final Set<String> parserNames) {
        consoleLogParsers = Sets.newHashSet(parserNames);
    }

    /**
     * Adds the specified parser configurations to this publisher.
     *
     * @param parserConfigurations
     *            the parser configurations to use
     */
    public void setParserConfigurations(final List<ParserConfiguration> parserConfigurations) {
        this.parserConfigurations = Lists.newArrayList(parserConfigurations);
    }

    /**
     * Returns the parserConfigurations.
     *
     * @return the parserConfigurations
     */
    public List<ParserConfiguration> getParserConfigurations() {
        return parserConfigurations;
    }

    /**
     * Upgrade for release 3.18 or older.
     *
     * @return this
     */
    @Override
    protected Object readResolve() {
        super.readResolve();

        if (consoleLogParsers == null || parserConfigurations == null) {
            consoleLogParsers = Sets.newHashSet();
            parserConfigurations = Lists.newArrayList();

            if (!ignoreConsole) {
                consoleLogParsers.addAll(parserNames);
            }
            if (StringUtils.isNotBlank(pattern)) {
                for (String parser : parserNames) {
                    parserConfigurations.add(new ParserConfiguration(pattern, parser));
                }
            }
        }
        return this;
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
        if (getConsoleLogParsers().isEmpty() && getParserConfigurations().isEmpty()) {
            throw new IOException("Error: No warning parsers defined.");
        }

        ParserResult project = parseFiles(build, logger);
        returnIfCanceled();

        parseConsoleLog(build, logger, project);

        project = build.getWorkspace().act(new AnnotationsClassifier(project, getDefaultEncoding()));
        for (FileAnnotation annotation : project.getAnnotations()) {
            annotation.setPathName(build.getWorkspace().getRemote());
        }

        WarningsResult result = new WarningsResult(build, getDefaultEncoding(), project);
        build.getActions().add(new WarningsResultAction(build, this, result));

        return result;
    }

    private void returnIfCanceled() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException("Canceling parsing since build has been aborted.");
        }
    }

    private void parseConsoleLog(final AbstractBuild<?, ?> build, final PluginLogger logger,
            final ParserResult project) throws IOException {
        if (!getConsoleLogParsers().isEmpty()) {
            logger.log("Parsing warnings in console log with parsers " + getConsoleLogParsers());
            ParserRegistry registry = new ParserRegistry(ParserRegistry.getParsers(getConsoleLogParsers()),
                    getDefaultEncoding(), getIncludePattern(), getExcludePattern());
            Collection<FileAnnotation> warnings = registry.parse(build.getLogFile(), logger);
            if (!build.getWorkspace().isRemote()) {
                String workspace = build.getWorkspace().getRemote();
                ModuleDetector detector = createModuleDetector(workspace);
                for (FileAnnotation annotation : warnings) {
                    String module = detector.guessModuleName(annotation.getFileName());
                    annotation.setModuleName(module);
                }
            }
            project.addAnnotations(warnings);
        }
    }

    private ParserResult parseFiles(final AbstractBuild<?, ?> build, final PluginLogger logger)
            throws IOException, InterruptedException {
        ParserResult project = new ParserResult(build.getWorkspace());
        for (ParserConfiguration configuration : getParserConfigurations()) {
            String filePattern = configuration.getPattern();
            String parserName = configuration.getParserName();
            logger.log("Parsing warnings in files '" + filePattern + "' with parser " + parserName);
            FilesParser parser = new FilesParser(new StringPluginLogger(PLUGIN_NAME), filePattern,
                    new FileWarningsParser(parserName, getDefaultEncoding(), getIncludePattern(), getExcludePattern()),
                    shouldDetectModules(), isMavenBuild(build));
            ParserResult additionalProject = build.getWorkspace().act(parser);
            logger.logLines(additionalProject.getLogMessages());
            project.addProject(additionalProject);

            returnIfCanceled();
        }
        return project;
    }

    private ModuleDetector createModuleDetector(final String workspace) {
        if (shouldDetectModules()) {
            return new ModuleDetector(new File(workspace));
        }
        else {
            return new NullModuleDetector();
        }
    }

    /** {@inheritDoc} */
    @Override
    public WarningsDescriptor getDescriptor() {
        return (WarningsDescriptor)super.getDescriptor();
    }

    /** {@inheritDoc} */
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
            final BuildListener listener) {
        return new WarningsAnnotationsAggregator(build, launcher, listener, this, getDefaultEncoding());
    }

    /** Name of parsers to use for scanning the logs. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient Set<String> parserNames = new HashSet<String>();
    /** Determines whether the console should be ignored. */
    private transient boolean ignoreConsole;
    /** Ant file-set pattern of files to work with. */
    private transient String pattern;
}
