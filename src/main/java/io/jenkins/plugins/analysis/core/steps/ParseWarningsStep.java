package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.Sets;

import io.jenkins.plugins.analysis.core.util.FilesParser;
import io.jenkins.plugins.analysis.core.steps.IssueParser.IssueParserDescriptor;
import jenkins.model.Jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.analysis.core.ParserResult;

/*
 TODO:

 - remove isMavenBuild from FilesParser
 - do we need the getters?
 */
public class ParseWarningsStep extends Step {
    private String defaultEncoding;
    private boolean shouldDetectModules;
    private IssueParser parser;
    private String pattern;

    @DataBoundConstructor
    public ParseWarningsStep() {
        // empty constructor required for Stapler
    }

    @CheckForNull
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the Ant file-set pattern of files to work with.
     *
     * @param pattern the pattern to use
     */
    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    @CheckForNull
    public IssueParser getParser() {
        return parser;
    }

    /**
     * Sets the parsers to use.
     *
     * @param parser the parser to use
     */
    @DataBoundSetter
    public void setParser(final IssueParser parser) {
        this.parser = parser;
    }

    public boolean getShouldDetectModules() {
        return shouldDetectModules;
    }

    /**
     * Enables or disables module scanning. If {@code shouldDetectModules} is set, then the module
     * name is derived by parsing Maven POM or Ant build files.
     *
     * @return shouldDetectModules if set to {@code true} then modules are scanned.
     */
    @DataBoundSetter
    public void setShouldDetectModules(final boolean shouldDetectModules) {
        this.shouldDetectModules = shouldDetectModules;
    }

    @CheckForNull
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public StepExecution start(final StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<ParserResult> {
        private final String defaultEncoding;
        private final boolean shouldDetectModules;
        private final IssueParser parser;
        private final String pattern;

        protected Execution(@Nonnull final StepContext context, final ParseWarningsStep step) {
            super(context);

            defaultEncoding = step.getDefaultEncoding();
            shouldDetectModules = step.getShouldDetectModules();
            parser = step.getParser();
            pattern = step.getPattern();
        }

        @Override
        protected ParserResult run() throws Exception {
            FilePath workspace = getContext().get(FilePath.class);
            TaskListener logger = getContext().get(TaskListener.class);

            logger.getLogger().append("Starting parser " + parser + " (encoding = " + defaultEncoding
                    + ", detectModules = " + shouldDetectModules + ") in workspace " + workspace + "\n");

            if (workspace != null) {
                ParserResult result = workspace.act(
                        new FilesParser(parser.getId(), getPattern(), parser, shouldDetectModules));
                logger.getLogger().append(result.getLogMessages());
                return result;
            }
            else {
                return new ParserResult();
            }
        }

        /** Maximum number of times that the environment expansion is executed. */
        private static final int RESOLVE_VARIABLES_DEPTH = 10;

        protected String getPattern() {
            return expandEnvironmentVariables(StringUtils.defaultIfBlank(pattern, parser.getDefaultPattern()));
        }

        /**
         * Resolve build parameters in the file pattern up to {@link #RESOLVE_VARIABLES_DEPTH} times.
         *
         * @param unexpanded the pattern to expand
         */
        private String expandEnvironmentVariables(final String unexpanded) {
            String expanded = unexpanded;
            try {
                EnvVars environment = getContext().get(EnvVars.class);
                if (environment != null && !environment.isEmpty()) {
                    for (int i = 0; i < RESOLVE_VARIABLES_DEPTH && StringUtils.isNotBlank(expanded); i++) {
                        String old = expanded;
                        expanded = Util.replaceMacro(expanded, environment);
                        if (old.equals(expanded)) {
                            break;
                        }
                    }
                }
            }
            catch (IOException e) {
                // ignore
            }
            catch (InterruptedException e) {
                // ignore
            }
            return expanded;
        }
    }

    @Extension
    public static class Descriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Sets.newHashSet(FilePath.class, EnvVars.class, TaskListener.class, Run.class);
        }

        @Override
        public String getFunctionName() {
            return "parseCoreWarnings";
        }

        @Override
        public String getDisplayName() {
            return "Parse warnings in files or in the console log";
        }

        public Collection<? extends IssueParserDescriptor> getAvailableParsers() {
            return Jenkins.getInstance().getDescriptorList(IssueParser.class);
        }
    }
}
