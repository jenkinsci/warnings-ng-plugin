package hudson.plugins.warnings;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.DynamicDocumentParser;
import hudson.plugins.warnings.parser.DynamicParser;
import hudson.plugins.warnings.parser.Warning;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Defines the properties of a warnings parser that uses a Groovy script to
 * parse the warnings log.
 *
 * @author Ulli Hafner
 */
public class GroovyParser extends AbstractDescribableImpl<GroovyParser> {
    private final String name;
    private final String regexp;
    private final String script;
    /** Example. @since 3.18 */
    private final String example;
    /** ProjectAction name. @since 4.0 */
    private String linkName;
    /** Trend report name. @since 4.0 */
    private String trendName;
    @CheckForNull
    private transient AbstractWarningsParser parser;

    /**
     * Creates a new instance of {@link GroovyParser}.
     *
     * @param name
     *            the name of the parser
     * @param regexp
     *            the regular expression
     * @param script
     *            the script to map the expression to a warning
     * @param example
     *            the example to verify the parser
     * @param linkName
     *            the name of the ProjectAction (link name)
     * @param trendName
     *            the name of the trend report
     */
    @DataBoundConstructor
    public GroovyParser(final String name, final String regexp, final String script, final String example,
            final String linkName, final String trendName) {
        super();

        this.name = name;
        this.regexp = regexp;
        this.script = script;
        this.example = example;
        this.linkName = linkName;
        this.trendName = trendName;
        parser = createParser();
    }

    /**
     * Creates a new instance of {@link GroovyParser}.
     *
     * @param name
     *            the name of the parser
     * @param regexp
     *            the regular expression
     * @param script
     *            the script to map the expression to a warning
     */
    public GroovyParser(final String name, final String regexp, final String script) {
        this(name, regexp, script, StringUtils.EMPTY, name, name);
    }

    /**
     * Adds link and trend names for 3.x serializations.
     *
     * @return the created object
     */
    private Object readResolve() {
        if (linkName == null) {
            linkName = Messages._Warnings_ProjectAction_Name().toString(Locale.ENGLISH);
        }
        if (trendName == null) {
            trendName = Messages._Warnings_Trend_Name().toString(Locale.ENGLISH);
        }
        parser = createParser();
        return this;
    }

    /**
     * Validates this instance.
     *
     * @return <code>true</code> if this instance is valid, <code>false</code>
     *         otherwise
     */
    public boolean isValid() {
        return parser != null;
    }

    private boolean canCreateParser() {
        DescriptorImpl d = new DescriptorImpl();
        return d.doCheckScript(script).kind == FormValidation.Kind.OK
                && d.doCheckRegexp(regexp).kind == FormValidation.Kind.OK
                && d.validate(name, Messages.Warnings_GroovyParser_Error_Name_isEmpty()).kind == FormValidation.Kind.OK;
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
     * Returns the trend name.
     *
     * @return the trend name
     */
    public String getTrendName() {
        return trendName;
    }

    /**
     * Returns the link name.
     *
     * @return the link name
     */
    public String getLinkName() {
        return linkName;
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
     * @since 3.18
     */
    public String getExample() {
        return StringUtils.defaultString(example);
    }

    /**
     * Returns whether the parser can scan messages spanning multiple lines.
     *
     * @return <code>true</code> if the parser can scan messages spanning
     *         multiple lines
     */
    public final boolean hasMultiLineSupport() {
        return containsNewline(regexp);
    }

    private static boolean containsNewline(final String expression) {
        return StringUtils.contains(expression, "\\n");
    }

    @CheckForNull
    private AbstractWarningsParser createParser() {
        if (canCreateParser()) {
            if (hasMultiLineSupport()) {
                return new DynamicDocumentParser(name, regexp, script, linkName, trendName);
            }
            else {
                return new DynamicParser(name, regexp, script, linkName, trendName);
            }
        }
        else {
            return null;
        }
    }

    /**
     * Returns a valid parser instance. If this parsers configuration is not valid, then <code>null</code> is returned.
     *
     * @return a valid parser instance or <code>null</code> if this parsers configuration is not valid.
     */
    @Nullable
    public AbstractWarningsParser getParser() {
        return parser;
    }

    /**
     * Descriptor to validate {@link GroovyParser}.
     *
     * @author Ulli Hafner
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<GroovyParser> {
        private static final String NEWLINE = "\n";
        private static final int MAX_MESSAGE_LENGTH = 60;

        private FormValidation validate(final String name, final String message) {
            if (StringUtils.isBlank(name)) {
                return FormValidation.error(message);
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation on the name of the parser that needs to be unique.
         *
         * @param name
         *            the name of the parser
         * @return the validation result
         */
        public FormValidation doCheckName(@QueryParameter(required = true) final String name) {
            return validate(name, Messages.Warnings_GroovyParser_Error_Name_isEmpty());
        }

        /**
         * Performs on-the-fly validation on the project action link name.
         *
         * @param linkName
         *            the link name
         * @return the validation result
         */
        public FormValidation doCheckLinkName(@QueryParameter(required = true) final String linkName) {
            return validate(linkName, Messages.Warnings_GroovyParser_Error_LinkName_isEmpty());
        }

        /**
         * Performs on-the-fly validation on the trend graph title.
         *
         * @param trendName
         *            the title of the trend graph
         * @return the validation result
         */
        public FormValidation doCheckTrendName(@QueryParameter(required = true) final String trendName) {
            return validate(trendName, Messages.Warnings_GroovyParser_Error_TrendName_isEmpty());
        }

        /**
         * Performs on-the-fly validation on the regular expression.
         *
         * @param regexp
         *            the regular expression
         * @return the validation result
         */
        public FormValidation doCheckRegexp(@QueryParameter(required = true) final String regexp) {
            try {
                if (StringUtils.isBlank(regexp)) {
                    return FormValidation.error(Messages.Warnings_GroovyParser_Error_Regexp_isEmpty());
                }
                Pattern.compile(regexp);

                return FormValidation.ok();
            }
            catch (PatternSyntaxException exception) {
                return FormValidation.error(Messages.Warnings_GroovyParser_Error_Regexp_invalid(exception.getLocalizedMessage()));
            }
        }

        /**
         * Performs on-the-fly validation on the Groovy script.
         *
         * @param script
         *            the script
         * @return the validation result
         */
        public FormValidation doCheckScript(@QueryParameter(required = true) final String script) {
            try {
                if (StringUtils.isBlank(script)) {
                    return FormValidation.error(Messages.Warnings_GroovyParser_Error_Script_isEmpty());
                }

                GroovyShell groovyShell = new GroovyShell(WarningsDescriptor.class.getClassLoader());
                groovyShell.parse(script);

                return FormValidation.ok();
            }
            catch (CompilationFailedException exception) {
                return FormValidation.error(Messages.Warnings_GroovyParser_Error_Script_invalid(exception.getLocalizedMessage()));
            }
        }

        /**
         * Parses the example message with the specified regular expression and script.
         *
         * @param example
         *            example that should be resolve to a warning
         * @param regexp
         *            the regular expression
         * @param script
         *            the script
         * @return the validation result
         */
        public FormValidation doCheckExample(@QueryParameter final String example,
                @QueryParameter final String regexp, @QueryParameter final String script) {
            if (StringUtils.isNotBlank(example) && StringUtils.isNotBlank(regexp) && StringUtils.isNotBlank(script)) {
                return parseExample(script, example, regexp, containsNewline(regexp));
            }
            else {
                return FormValidation.ok();
            }
        }

        /**
         * Parses the example and returns a validation result of type
         * {@link Kind#OK} if a warning has been found.
         *
         * @param script
         *            the script that parses the expression
         * @param example
         *            example text that will be matched by the regular expression
         * @param regexp
         *            the regular expression
         * @param hasMultiLineSupport
         *            determines whether multi-lines support is activated
         * @return a result of {@link Kind#OK} if a warning has been found
         */
        private FormValidation parseExample(final String script, final String example, final String regexp, final boolean hasMultiLineSupport) {
            Pattern pattern;
            if (hasMultiLineSupport) {
                pattern = Pattern.compile(regexp, Pattern.MULTILINE);
            }
            else {
                pattern = Pattern.compile(regexp);
            }
            Matcher matcher = pattern.matcher(example);
            if (matcher.find()) {
                Binding binding = new Binding();
                binding.setVariable("matcher", matcher);
                GroovyShell shell = new GroovyShell(WarningsDescriptor.class.getClassLoader(), binding);
                Object result = null;
                try {
                    result = shell.evaluate(script);
                }
                catch (Exception exception) { // NOCHECKSTYLE: catch all exceptions of the Groovy script
                    return FormValidation.error(
                            Messages.Warnings_GroovyParser_Error_Example_exception(exception.getMessage()));
                }
                if (result instanceof Warning) {
                    StringBuilder okMessage = new StringBuilder(Messages.Warnings_GroovyParser_Error_Example_ok_title());
                    Warning warning = (Warning)result;
                    message(okMessage, Messages.Warnings_GroovyParser_Error_Example_ok_file(warning.getFileName()));
                    message(okMessage, Messages.Warnings_GroovyParser_Error_Example_ok_line(warning.getPrimaryLineNumber()));
                    message(okMessage, Messages.Warnings_GroovyParser_Error_Example_ok_priority(warning.getPriority().getLongLocalizedString()));
                    message(okMessage, Messages.Warnings_GroovyParser_Error_Example_ok_category(warning.getCategory()));
                    message(okMessage, Messages.Warnings_GroovyParser_Error_Example_ok_type(warning.getType()));
                    message(okMessage, Messages.Warnings_GroovyParser_Error_Example_ok_message(warning.getMessage()));
                    return FormValidation.ok(okMessage.toString());
                }
                else {
                    return FormValidation.error(Messages.Warnings_GroovyParser_Error_Example_wrongReturnType(result));
                }
            }
            else {
                return FormValidation.error(Messages.Warnings_GroovyParser_Error_Example_regexpDoesNotMatch());
            }
        }

        private void message(final StringBuilder okMessage, final String message) {
            okMessage.append(NEWLINE);
            int max = MAX_MESSAGE_LENGTH;
            if (message.length() > max) {
                int size = max / 2 - 1;
                okMessage.append(message.substring(0, size));
                okMessage.append("[...]");
                okMessage.append(message.substring(message.length() - size, message.length()));
            }
            else {
                okMessage.append(message);
            }
        }

        @Override
        public String getDisplayName() {
            return StringUtils.EMPTY;
        }
    }
}

