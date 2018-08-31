package hudson.plugins.warnings; // NOPMD

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.AnnotationsClassifier;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.NullBuildHistory;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.NullModuleDetector;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.parser.FileWarningsParser;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.plugins.warnings.parser.ParsingCanceledException;
import hudson.plugins.warnings.parser.WarningsFilter;

/**
 * Publishes the results of the warnings analysis (freestyle project type).
 *
 * @author Ullrich Hafner
 * @deprecated replaced by classes of io.jenkins.plugins.analysis package
 */
@Deprecated
// CHECKSTYLE:COUPLING-OFF
public class WarningsPublisher extends HealthAwarePublisher {
    private static final long serialVersionUID = -5936973521277401764L;

    private static final String PLUGIN_NAME = "WARNINGS";

    /** Ant file-set pattern of files to include to report. */
    private String includePattern;
    /** Ant file-set pattern of files to exclude from report. */
    private String excludePattern;
    /** warning messages to exclude from report */
    private String messagesPattern;
    /** warning categories to exclude from report */
    private String categoriesPattern;
	
    /** File pattern and parser configurations. @since 3.19 */
    @SuppressFBWarnings("SE")
    private List<ParserConfiguration> parserConfigurations = Lists.newArrayList();
    /** Parser configurations of the console. @since 4.6 */
    @SuppressFBWarnings("SE")
    private List<ConsoleParser> consoleParsers = Lists.newArrayList();

    /**
     * Creates a new instance of {@link WarningsPublisher}.
     */
    @DataBoundConstructor
    public WarningsPublisher() {
        super(PLUGIN_NAME);
    }

    /**
     * Returns the names of the configured parsers for the console log.
     *
     * @return the parser names
     */
    public ConsoleParser[] getConsoleParsers() {
        return ConsoleParser.filterExisting(consoleParsers);
    }

    /**
     * Sets the Ant file-set pattern of files to include in report.
     *
     * @param consoleParsers
     *            the parsers to scan the console
     */
    @DataBoundSetter
    public void setConsoleParsers(final ConsoleParser[] consoleParsers) {
        if (consoleParsers != null) {
            this.consoleParsers.addAll(Arrays.asList(consoleParsers));
        }
    }

    /**
     * Returns the parserConfigurations.
     *
     * @return the parserConfigurations
     */
    public ParserConfiguration[] getParserConfigurations() {
        return ParserConfiguration.filterExisting(parserConfigurations);
    }

    /**
     * Sets the Ant file-set pattern of files to include in report.
     *
     * @param parserConfigurations
     *            the parser configurations to scan files
     */
    @DataBoundSetter
    public void setParserConfigurations(final ParserConfiguration[] parserConfigurations) {
        if (parserConfigurations != null) {
            this.parserConfigurations.addAll(Arrays.asList(parserConfigurations));
        }
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
     * Sets the Ant file-set pattern of files to include in report.
     *
     * @param pattern the pattern to include
     */
    @DataBoundSetter
    public void setIncludePattern(final String pattern) {
        includePattern = pattern;
    }

    /**
     * Returns the Ant file-set pattern of files to exclude from report.
     *
     * @return Ant file-set pattern of files to exclude from report
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    /**
     * Sets the Ant file-set pattern of files to exclude from report.
     *
     * @param pattern the pattern to exclude
     */
    @DataBoundSetter
    public void setExcludePattern(final String pattern) {
        excludePattern = pattern;
    }
	
    /**
     * Returns the Java regex pattern of warning messages to exclude from report.
     *
     * @return the Java regex pattern of warning messages to exclude from report
     */
    public String getMessagesPattern() {
        return messagesPattern;
    }

    /**
     * Returns the Java regex pattern of warning categories to exclude from report.
     *
     * @return the Java regex pattern of warning categories to exclude from report
     */
    public String getCategoriesPattern() {
        return categoriesPattern;
    }

    /**
     * Sets the Java regex pattern of warning messages to exclude from report.
     *
     * @param pattern the pattern to exclude
     */
    @DataBoundSetter
    public void setMessagesPattern(final String pattern) {
        messagesPattern = pattern;
    }

    /**
     * Sets the Java regex pattern of warning categories to exclude from report.
     *
     * @param pattern the pattern to exclude
     */
    @DataBoundSetter
    public void setCategoriesPattern(final String pattern) {
        categoriesPattern = pattern;
    }

    /**
     * Upgrade for release 4.5 or older.
     *
     * @return this
     */
    @Override
    protected Object readResolve() {
        super.readResolve();

        if (consoleParsers == null) {
            consoleParsers = Lists.newArrayList();

            if (isOlderThanRelease318()) {
                upgradeFrom318();
            }

            for (String  parser : consoleLogParsers) {
                consoleParsers.add(new ConsoleParser(parser));
            }
        }

        replaceConsoleParsersWithChangedName();
        replaceFileParsersWithChangedName();

        return this;
    }

    private void replaceConsoleParsersWithChangedName() {
        List<ConsoleParser> updatedConsoleParsers = new ArrayList<>(consoleParsers);
        for (ConsoleParser parser : consoleParsers) {
            String parserName = parser.getParserName();
            if (ParserRegistry.exists(parserName)) {
                String group = getGroup(parserName);
                if (!group.equals(parserName)) {
                    updatedConsoleParsers.remove(parser);
                    updatedConsoleParsers.add(new ConsoleParser(group));
                }
            }
            consoleParsers = updatedConsoleParsers;
        }
    }

    private void replaceFileParsersWithChangedName() {
        List<ParserConfiguration> updatedFileParsers = new ArrayList<>(parserConfigurations);
        for (ParserConfiguration parser : parserConfigurations) {
            String parserName = parser.getParserName();
            if (ParserRegistry.exists(parserName)) {
                String group = getGroup(parserName);
                if (!group.equals(parserName)) {
                    updatedFileParsers.remove(parser);
                    updatedFileParsers.add(new ParserConfiguration(parser.getPattern(), group));
                }
            }
            parserConfigurations = updatedFileParsers;
        }
    }

    private String getGroup(final String parserName) {
        return ParserRegistry.getParser(parserName).getGroup();
    }

    private void upgradeFrom318() {
        consoleLogParsers = Sets.newHashSet();
        parserConfigurations = Lists.newArrayList();

        if (parserNames != null) {
            convertToNewFormat();
        }
    }

    private boolean isOlderThanRelease318() {
        return consoleLogParsers == null || parserConfigurations == null;
    }

    private void convertToNewFormat() {
        if (!ignoreConsole) {
            consoleLogParsers.addAll(parserNames);
        }
        if (StringUtils.isNotBlank(pattern)) {
            for (String parser : parserNames) {
                parserConfigurations.add(new ParserConfiguration(pattern, parser));
            }
        }
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        throw new IllegalStateException("Not available since release 4.0.");
    }

    private List<String> getParsers() {
        List<String> parsers = Lists.newArrayList();
        for (ConsoleParser configuration : getConsoleParsers()) {
            parsers.add(configuration.getParserName());
        }
        for (ParserConfiguration configuration : getParserConfigurations()) {
            parsers.add(configuration.getParserName());
        }
        return parsers;
    }


    @Override
    protected BuildResult perform(final Run<?, ?> run, final FilePath workspace, final PluginLogger logger)
            throws InterruptedException, IOException {
        try {
            if (!hasConsoleParsers() && !hasFileParsers()) {
                throw new IOException("Error: No warning parsers defined in the job configuration.");
            }

            List<ParserResult> fileResults = parseFiles(run, workspace, logger);
            List<ParserResult> consoleResults = parseConsoleLog(run, workspace, logger);

            ParserResult totals = new ParserResult();
            add(totals, consoleResults);
            add(totals, fileResults);

            BuildHistory history = new BuildHistory(run, AggregatedWarningsResultAction.class,
                    usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
            AggregatedWarningsResult result = new AggregatedWarningsResult(run, history, totals, getDefaultEncoding());
            run.addAction(new AggregatedWarningsResultAction(run, result));

            return result;
        }
        catch (ParsingCanceledException exception) {
            return emptyBuildResult(run, logger, exception);
        }
        catch (InterruptedException exception) {
            return emptyBuildResult(run, logger, exception);
        }
    }

    private BuildResult emptyBuildResult(final Run<?, ?> run, final PluginLogger logger, final Exception exception) {
        logger.log(exception.getMessage());

        return new AggregatedWarningsResult(run, new NullBuildHistory(), new ParserResult(), getDefaultEncoding());
    }

    private boolean hasFileParsers() {
        return getParserConfigurations().length > 0;
    }

    private boolean hasConsoleParsers() {
        return getConsoleParsers().length > 0;
    }

    private void add(final ParserResult totals, final List<ParserResult> results) {
        for (ParserResult result : results) {
            totals.addProject(result);
        }
    }

    private InterruptedException createInterruptedException() {
        return new InterruptedException("Canceling parsing since build has been aborted.");
    }

    private void returnIfCanceled() throws InterruptedException {
        if (Thread.interrupted()) {
            throw createInterruptedException();
        }
    }

    private List<ParserResult> parseConsoleLog(final Run<?, ?> run, final FilePath workspace, final PluginLogger logger)
            throws IOException, InterruptedException {
        List<ParserResult> results = Lists.newArrayList();
        for (ConsoleParser parser : getConsoleParsers()) {
            String parserName = parser.getParserName();
            logger.log("Parsing warnings in console log with parser " + parserName);

            Collection<FileAnnotation> warnings = new ParserRegistry(ParserRegistry.getParsers(parserName),
                    getDefaultEncoding()).parse(run.getLogFile());
            if (!workspace.isRemote()) {
                guessModuleNames(workspace, warnings);
            }
            ParserResult project = new ParserResult(workspace, canResolveRelativePaths());
            project.addAnnotations(warnings);

            results.add(annotate(run, workspace, filterWarnings(project, logger), parserName));
        }
        return results;
    }

    private ParserResult filterWarnings(final ParserResult project, final PluginLogger logger) {
        WarningsFilter filter = new WarningsFilter();
        if (filter.isActive(getIncludePattern(), getExcludePattern(), getMessagesPattern(), getCategoriesPattern())) {
            Collection<FileAnnotation> filtered = filter.apply(project.getAnnotations(),
                    getIncludePattern(), getExcludePattern(), getMessagesPattern(), getCategoriesPattern(), logger);
            return new ParserResult(filtered);
        }
        return project;
    }

    private void guessModuleNames(final FilePath workspace, final Collection<FileAnnotation> warnings) {
        ModuleDetector detector = createModuleDetector(workspace.getRemote());
        for (FileAnnotation annotation : warnings) {
            String module = detector.guessModuleName(annotation.getFileName());
            annotation.setModuleName(module);
        }
    }

    private List<ParserResult> parseFiles(final Run<?, ?> run, final FilePath workspace, final PluginLogger logger)
            throws IOException, InterruptedException {
        List<ParserResult> results = Lists.newArrayList();
        for (ParserConfiguration configuration : getParserConfigurations()) {
            String filePattern = expandFilePattern(configuration.getPattern(), run.getEnvironment(TaskListener.NULL));
            String parserName = configuration.getParserName();

            logger.log("Parsing warnings in files '" + filePattern + "' with parser " + parserName);

            FilesParser parser = new FilesParser(PLUGIN_NAME, filePattern,
                    new FileWarningsParser(ParserRegistry.getParsers(parserName), getDefaultEncoding()),
                    shouldDetectModules(), isMavenBuild(run), canResolveRelativePaths());
            ParserResult project = workspace.act(parser);
            logger.logLines(project.getLogMessages());

            returnIfCanceled();

            results.add(annotate(run, workspace, filterWarnings(project, logger), configuration.getParserName()));
        }
        return results;
    }

    private ParserResult annotate(final Run<?, ?> run, final FilePath workspace, final ParserResult input, final String parserName)
            throws IOException, InterruptedException {
        ParserResult output = workspace.act(new AnnotationsClassifier(input, getDefaultEncoding()));
        for (FileAnnotation annotation : output.getAnnotations()) {
            annotation.setPathName(workspace.getRemote());
        }
        WarningsBuildHistory history = new WarningsBuildHistory(run, parserName,
                usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());

        blame(output.getAnnotations(), run, workspace);

        WarningsResult result = new WarningsResult(run, history, output, getDefaultEncoding(), parserName);
        run.addAction(new WarningsResultAction(run, this, result, parserName));

        return output;
    }

    private ModuleDetector createModuleDetector(final String workspace) {
        if (shouldDetectModules()) {
            return new ModuleDetector(new File(workspace));
        }
        else {
            return new NullModuleDetector();
        }
    }

    @Override
    public WarningsDescriptor getDescriptor() {
        return (WarningsDescriptor)super.getDescriptor();
    }
 
    @Override
    public MatrixAggregator createAggregator(final MatrixBuild run, final Launcher launcher, final BuildListener listener) {
        return new WarningsAnnotationsAggregator(run, launcher, listener, this, getDefaultEncoding(),
                usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
    }

    /** Name of parsers to use for scanning the logs. */
    @SuppressWarnings("PMD")
    private transient Set<String> parserNames;
    /** Determines whether the console should be ignored. */
    @SuppressWarnings("PMD")
    private transient boolean ignoreConsole;
    /** Ant file-set pattern of files to work with. */
    @SuppressWarnings("PMD")
    private transient String pattern;
    /** Parser to scan the console log. @since 3.19 */
    private transient Set<String> consoleLogParsers;
}
