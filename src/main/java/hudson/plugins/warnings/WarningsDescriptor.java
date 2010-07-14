package hudson.plugins.warnings;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.warnings.parser.Warning;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link WarningsPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
@Extension(ordinal = 100) // NOCHECKSTYLE
public final class WarningsDescriptor extends PluginDescriptor {
    /** Plug-in name. */
    private static final String PLUGIN_NAME = "warnings";
    /** Icon to use for the result and project action. */
    private static final String ACTION_ICON = "/plugin/warnings/icons/warnings-24x24.png";

    private final CopyOnWriteList<GroovyParser> groovyParsers = new CopyOnWriteList<GroovyParser>();

    /**
     * Instantiates a new {@link WarningsDescriptor}.
     */
    public WarningsDescriptor() {
        super(WarningsPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.Warnings_Publisher_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public String getIconUrl() {
        return ACTION_ICON;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public WarningsPublisher newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        Set<String> parsers = extractParsers(formData);

        WarningsPublisher publisher = request.bindJSON(WarningsPublisher.class, formData);
        publisher.setParserNames(parsers);

        return publisher;
    }

    /**
     * Extract the list of parsers to use from the JSON form data.
     *
     * @param formData
     *            the JSON form data
     * @return the list of parsers to use
     */
    private Set<String> extractParsers(final JSONObject formData) {
        Set<String> parsers = new HashSet<String>();
        Object values = formData.get("parsers");
        if (values instanceof JSONArray) {
            JSONArray array = (JSONArray)values;
            for (int i = 0; i < array.size(); i++) {
                JSONObject element = array.getJSONObject(i);
                parsers.add(element.getString("parserName"));
            }
            formData.remove("parsers");
        }
        else if (values instanceof JSONObject) {
            JSONObject object = (JSONObject)values;
            parsers.add(object.getString("parserName"));
            formData.remove("parsers");
        }

        return parsers;
    }

    /**
     * Returns the configured Groovy parsers.
     *
     * @return the Groovy parsers
     */
    public CopyOnWriteList<GroovyParser> getParsers() {
        return groovyParsers;
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) {
        groovyParsers.replaceBy(req.bindJSONToList(GroovyParser.class, formData.get("parsers")));

        return true;
    }

    /**
     * Performs on-the-fly validation on the name of the parser that needs to be unique.
     *
     * @param name
     *            the name of the parser
     * @return the validation result
     */
    public FormValidation doCheckName(@QueryParameter(required = true) final String name) {
        return GroovyParser.doCheckName(name);
    }

    /**
     * Performs on-the-fly validation on the regular expression.
     *
     * @param regexp
     *            the regular expression
     * @return the validation result
     */
    public FormValidation doCheckRegexp(@QueryParameter(required = true) final String regexp) {
        return GroovyParser.doCheckRegexp(regexp);
    }

    /**
     * Performs on-the-fly validation on the Groovy script.
     *
     * @param script
     *            the script
     * @return the validation result
     */
    public FormValidation doCheckScript(@QueryParameter(required = true) final String script) {
        return GroovyParser.doCheckScript(script);
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
            return parseExample(script, example, regexp);
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
     * @return a result of {@link Kind#OK} if a warning has been found
     */
    private FormValidation parseExample(final String script, final String example, final String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(example);
        if (matcher.matches()) {
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
                return FormValidation.ok(
                        Messages.Warnings_GroovyParser_Error_Example_ok(result.toString()));
            }
            else {
                return FormValidation.error(Messages.Warnings_GroovyParser_Error_Example_wrongReturnType(result));
            }
        }
        else {
            return FormValidation.error(Messages.Warnings_GroovyParser_Error_Example_regexpDoesNotMatch());
        }
    }
}