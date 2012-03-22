package hudson.plugins.warnings;

import groovy.lang.GroovyShell;
import hudson.util.FormValidation;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Defines the properties of a warnings parser that uses a Groovy script to
 * parse the warnings log.
 *
 * @author Ulli Hafner
 */
public class GroovyParser {
    private final String name;
    private final String regexp;
    private final String script;
    /** Example. @since 3.18 */
    private final String example;
    /** ProjectAction name. @since 4.0 */
    private String linkName;
    /** Trend report name. @since 4.0 */
    private String trendName;

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
        this.name = name;
        this.regexp = regexp;
        this.script = script;
        this.example = example;
        this.linkName = linkName;
        this.trendName = trendName;
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

        return this;
    }

    /**
     * Validates this instance.
     *
     * @return <code>true</code> if this instance is valid, <code>false</code>
     *         otherwise
     */
    public boolean isValid() {
        return doCheckScript(script).kind == FormValidation.Kind.OK
                && doCheckRegexp(regexp).kind == FormValidation.Kind.OK
                && doCheckName(name).kind == FormValidation.Kind.OK;
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
    public boolean hasMultiLineSupport() {
        return StringUtils.isNotBlank(regexp) && regexp.contains("\\n");
    }

    private static FormValidation validate(final String name, final String message) {
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
    public static FormValidation doCheckName(final String name) {
        return validate(name, Messages.Warnings_GroovyParser_Error_Name_isEmpty());
    }

    /**
     * Performs on-the-fly validation on the link name of the parser.
     *
     * @param name
     *            the name of the link
     * @return the validation result
     */
    public static FormValidation doCheckLinkName(final String name) {
        return validate(name, Messages.Warnings_GroovyParser_Error_LinkName_isEmpty());
    }

    /**
     * Performs on-the-fly validation on the trend name of the parser.
     *
     * @param name
     *            the name of the trend
     * @return the validation result
     */
    public static FormValidation doCheckTrendName(final String name) {
        return validate(name, Messages.Warnings_GroovyParser_Error_TrendName_isEmpty());
    }

    /**
     * Performs on-the-fly validation on the regular expression.
     *
     * @param regexp
     *            the regular expression
     * @return the validation result
     */
    public static FormValidation doCheckRegexp(final String regexp) {
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
    public static FormValidation doCheckScript(final String script) {
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
}

