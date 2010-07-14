package hudson.plugins.warnings;

import groovy.lang.GroovyShell;
import hudson.util.FormValidation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Defines the properties of a warnings parser that uses a groovy script to
 * parse the warnings log.
 *
 * @author Ulli Hafner
 */
public class GroovyParser {
    private final String name;
    private final String regexp;
    private final String script;

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
    @DataBoundConstructor
    public GroovyParser(final String name, final String regexp, final String script) {
        this.name = name;
        this.regexp = regexp;
        this.script = script;
    }

    public boolean isValid() {
        return doCheckScript(script).kind == FormValidation.Kind.OK
                && doCheckRegexp(regexp).kind == FormValidation.Kind.OK
                && doCheckName(regexp).kind == FormValidation.Kind.OK;
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
     * Performs on-the-fly validation on the name of the parser that needs to be unique.
     *
     * @param name
     *            the name of the parser
     * @return the validation result
     */
    public static FormValidation doCheckName(final String name) {
        if (StringUtils.isBlank(name)) {
            return FormValidation.error(Messages.Warnings_GroovyParser_Error_Name_isEmpty());
        }
        return FormValidation.ok();
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

