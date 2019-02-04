package io.jenkins.plugins.analysis.warnings.groovy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.util.Ensure;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import groovy.lang.Script;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

/**
 * Defines the properties of a warnings parser that uses a Groovy script to parse the warnings log.
 *
 * @author Ullrich Hafner
 */
public class GroovyParser extends AbstractDescribableImpl<GroovyParser> implements Serializable {
    private static final long serialVersionUID = 2447124045452896581L;
    private static final int MAX_EXAMPLE_SIZE = 4096;

    private final String id;
    private final String name;
    private final String regexp;
    private final String script;
    private final String example;

    @SuppressFBWarnings("SE")
    private transient JenkinsFacade jenkinsFacade = new JenkinsFacade();

    /**
     * Creates a new instance of {@link GroovyParser}.
     *
     * @param id
     *         the ID of the parser
     * @param name
     *         the name of the parser
     * @param regexp
     *         the regular expression
     * @param script
     *         the script to map the expression to a warning
     * @param example
     *         the example to verify the parser
     */
    @DataBoundConstructor
    public GroovyParser(final String id, final String name,
            final String regexp, final String script, final String example) {
        super();

        this.id = id;
        this.name = name;
        this.regexp = regexp;
        this.script = script;
        this.example = example.length() > MAX_EXAMPLE_SIZE ? example.substring(0, MAX_EXAMPLE_SIZE) : example;
    }

    private static boolean containsNewline(final String expression) {
        return StringUtils.contains(expression, "\\n") || StringUtils.contains(expression, "\\r");
    }

    /**
     * Validates this instance.
     *
     * @return {@code true} if this instance is valid, {@code false} otherwise
     */
    public boolean isValid() {
        DescriptorImpl d = new DescriptorImpl(getJenkinsFacade());

        return d.doCheckScript(script).kind == Kind.OK
                && d.doCheckRegexp(regexp).kind == Kind.OK
                && d.validate(name, Messages.GroovyParser_Error_Name_isEmpty()).kind == Kind.OK;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the regular expression.
     *
     * @return the regular expression
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * Returns the Groovy script.
     *
     * @return the Groovy script
     */
    public String getScript() {
        return script;
    }

    /**
     * Returns the example to verify the parser.
     *
     * @return the example
     */
    public String getExample() {
        return StringUtils.defaultString(example);
    }

    /**
     * Returns whether the parser can scan messages spanning multiple lines.
     *
     * @return {@code true} if the parser can scan messages spanning multiple lines
     */
    public final boolean hasMultiLineSupport() {
        return containsNewline(regexp);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroovyParser that = (GroovyParser) o;

        if (!regexp.equals(that.regexp)) {
            return false;
        }
        return script.equals(that.script);
    }

    @Override
    public int hashCode() {
        int result = regexp.hashCode();
        result = 31 * result + script.hashCode();
        return result;
    }

    /**
     * Returns a new parser instance.
     *
     * @return a new parser instance
     * @throws AssertionError
     *         if this parsers configuration is not valid
     */
    public IssueParser createParser() {
        Ensure.that(isValid()).isTrue();

        if (hasMultiLineSupport()) {
            return new DynamicDocumentParser(regexp, script);
        }
        else {
            return new DynamicLineParser(regexp, script);
        }
    }

    ReportScanningTool toStaticAnalysisTool() {
        return new GroovyParserToolAdapter(this);
    }

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
    }

    private JenkinsFacade getJenkinsFacade() {
        return ObjectUtils.defaultIfNull(jenkinsFacade, new JenkinsFacade());
    }

    /**
     * Descriptor to validate {@link GroovyParser}.
     *
     * @author Ullrich Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<GroovyParser> {
        private static final String NEWLINE = "\n";
        private static final int MAX_MESSAGE_LENGTH = 60;
        private static final FormValidation NO_RUN_SCRIPT_PERMISSION_WARNING
                = FormValidation.warning(Messages.GroovyParser_Warning_NoRunScriptPermission());
        private final JenkinsFacade jenkinsFacade;

        /**
         * Creates a new descriptor.
         */
        @SuppressWarnings("unused") // Called by Jenkins
        public DescriptorImpl() {
            this(new JenkinsFacade());
        }

        DescriptorImpl(final JenkinsFacade jenkinsFacade) {
            super();
            
            this.jenkinsFacade = jenkinsFacade;
        }

        private FormValidation validate(final String name, final String message) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error(message);
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the parser ID. The ID needs to be unique.
         *
         * @param id
         *         the ID of the parser
         *
         * @return the validation result
         */
        public FormValidation doCheckId(@QueryParameter(required = true) final String id) {
            if (StringUtils.isBlank(id)) {
                return FormValidation.error(Messages.GroovyParser_Error_Id_isEmpty());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation on the name of the parser that needs to be unique.
         *
         * @param name
         *         the name of the parser
         *
         * @return the validation result
         */
        public FormValidation doCheckName(@QueryParameter(required = true) final String name) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error(Messages.GroovyParser_Error_Name_isEmpty());
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation on the regular expression.
         *
         * @param regexp
         *         the regular expression
         *
         * @return the validation result
         */
        public FormValidation doCheckRegexp(@QueryParameter(required = true) final String regexp) {
            try {
                if (StringUtils.isBlank(regexp)) {
                    return FormValidation.error(Messages.GroovyParser_Error_Regexp_isEmpty());
                }
                Pattern pattern = Pattern.compile(regexp);
                Ensure.that(pattern).isNotNull();

                return FormValidation.ok();
            }
            catch (PatternSyntaxException exception) {
                return FormValidation.error(
                        Messages.GroovyParser_Error_Regexp_invalid(exception.getLocalizedMessage()));
            }
        }

        /**
         * Performs on-the-fly validation on the Groovy script.
         *
         * @param script
         *         the script
         *
         * @return the validation result
         */
        @RequirePOST
        public FormValidation doCheckScript(@QueryParameter(required = true) final String script) {
            if (isNotAllowedToRunScripts()) {
                return NO_RUN_SCRIPT_PERMISSION_WARNING;
            }
            try {
                if (StringUtils.isBlank(script)) {
                    return FormValidation.error(Messages.GroovyParser_Error_Script_isEmpty());
                }

                GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(script);
                Script compiled = matcher.compile();
                Ensure.that(compiled).isNotNull();

                return FormValidation.ok();
            }
            catch (CompilationFailedException exception) {
                return FormValidation.error(
                        Messages.GroovyParser_Error_Script_invalid(exception.getLocalizedMessage()));
            }
        }

        private boolean isNotAllowedToRunScripts() {
            return !jenkinsFacade.hasPermission(Jenkins.RUN_SCRIPTS);
        }

        /**
         * Parses the example message with the specified regular expression and script.
         *
         * @param example
         *         example that should be resolve to a warning
         * @param regexp
         *         the regular expression
         * @param script
         *         the script
         *
         * @return the validation result
         */
        @RequirePOST
        public FormValidation doCheckExample(@QueryParameter final String example,
                @QueryParameter final String regexp, @QueryParameter final String script) {
            if (isNotAllowedToRunScripts()) {
                return NO_RUN_SCRIPT_PERMISSION_WARNING;
            }
            if (StringUtils.isNotBlank(example) && StringUtils.isNotBlank(regexp) && StringUtils.isNotBlank(script)) {
                FormValidation response = parseExample(script, example, regexp, containsNewline(regexp));
                if (example.length() <= MAX_EXAMPLE_SIZE) {
                    return response;
                }
                return FormValidation.aggregate(Arrays.asList(
                        FormValidation.warning(Messages.GroovyParser_long_examples_will_be_truncated()), response));
            }
            else {
                return FormValidation.ok();
            }
        }

        /**
         * Parses the example and returns a validation result of type {@link Kind#OK} if a warning has been found.
         *
         * @param script
         *         the script that parses the expression
         * @param example
         *         example text that will be matched by the regular expression
         * @param regexp
         *         the regular expression
         * @param hasMultiLineSupport
         *         determines whether multi-lines support is activated
         *
         * @return a result of {@link Kind#OK} if a warning has been found
         */
        @SuppressWarnings("illegalcatch")
        private FormValidation parseExample(final String script, final String example, final String regexp,
                final boolean hasMultiLineSupport) {
            Pattern pattern;
            if (hasMultiLineSupport) {
                pattern = Pattern.compile(regexp, Pattern.MULTILINE);
            }
            else {
                pattern = Pattern.compile(regexp);
            }
            Matcher matcher = pattern.matcher(example);
            try {
                if (matcher.find()) {
                    GroovyExpressionMatcher checker = new GroovyExpressionMatcher(script);
                    Object result = checker.run(matcher, new IssueBuilder(), 0, "UI Example");
                    Optional<?> optional = (Optional) result;
                    if (optional.isPresent()) {
                        Object wrappedIssue = optional.get();
                        if (wrappedIssue instanceof Issue) {
                            return createOkMessage((Issue) wrappedIssue);
                        }
                    }
                    return FormValidation.error(Messages.GroovyParser_Error_Example_wrongReturnType(result));
                }
                else {
                    return FormValidation.error(Messages.GroovyParser_Error_Example_regexpDoesNotMatch());
                }
            }
            catch (Exception exception) { // catch all exceptions of the Groovy script
                return FormValidation.error(
                        Messages.GroovyParser_Error_Example_exception(exception.getMessage()));
            }
        }

        private FormValidation createOkMessage(final Issue issue) {
            StringBuilder okMessage = new StringBuilder(Messages.GroovyParser_Error_Example_ok_title());
            message(okMessage, Messages.GroovyParser_Error_Example_ok_file(issue.getFileName()));
            message(okMessage, Messages.GroovyParser_Error_Example_ok_line(issue.getLineStart()));
            message(okMessage, Messages.GroovyParser_Error_Example_ok_priority(issue.getSeverity()));
            message(okMessage, Messages.GroovyParser_Error_Example_ok_category(issue.getCategory()));
            message(okMessage, Messages.GroovyParser_Error_Example_ok_type(issue.getType()));
            message(okMessage, Messages.GroovyParser_Error_Example_ok_message(issue.getMessage()));
            return FormValidation.ok(okMessage.toString());
        }

        private void message(final StringBuilder okMessage, final String message) {
            okMessage.append(NEWLINE);
            int max = MAX_MESSAGE_LENGTH;
            if (message.length() > max) {
                int size = max / 2 - 1;
                okMessage.append(message, 0, size);
                okMessage.append("[...]");
                okMessage.append(message, message.length() - size, message.length());
            }
            else {
                okMessage.append(message);
            }
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }
    }
}

