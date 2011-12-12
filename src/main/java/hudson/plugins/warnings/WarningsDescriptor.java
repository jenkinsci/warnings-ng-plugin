package hudson.plugins.warnings;

import static hudson.plugins.analysis.core.PluginDescriptor.*;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.warnings.parser.Warning;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
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
    private static final String CONSOLE_LOG_PARSERS_KEY = "consoleLogParsers";
    private static final String FILE_LOCATIONS_KEY = "locations";
    private static final String PARSER_NAME_ATTRIBUTE = "parserName";
    private static final String PLUGIN_NAME = "warnings";
    private static final String NEWLINE = "\n";
    private static final int MAX_MESSAGE_LENGTH = 60;

    /** Icon to use for the result and project action. */
    private static final String ACTION_ICON = "/plugin/warnings/icons/warnings-24x24.png";

    private final CopyOnWriteList<GroovyParser> groovyParsers = new CopyOnWriteList<GroovyParser>();

    /**
     * Instantiates a new {@link WarningsDescriptor}.
     */
    public WarningsDescriptor() {
        this(true);
    }

    /**
     * Instantiates a new {@link WarningsDescriptor}.
     *
     * @param loadConfiguration
     *            determines whether the values of this instance should be
     *            loaded from disk
     */
    public WarningsDescriptor(final boolean loadConfiguration) {
        super(WarningsPublisher.class);

        if (loadConfiguration) {
            load();
        }
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
        JSONObject flattenedData = convertHierarchicalFormData(formData);
        Set<String> consoleLogParsers = extractConsoleLogParsers(flattenedData);
        List<ParserConfiguration> parserConfigurations = request.bindJSONToList(ParserConfiguration.class, flattenedData.get(FILE_LOCATIONS_KEY));

        WarningsPublisher publisher = request.bindJSON(WarningsPublisher.class, flattenedData);
        publisher.setConsoleLogParsers(consoleLogParsers);
        publisher.setParserConfigurations(parserConfigurations);

        return publisher;
    }

    /**
     * Extract the list of locations and associated parsers from the JSON form data.
     *
     * @param formData
     *            the JSON form data
     * @return the list of parsers to use
     */
    private Set<String> extractConsoleLogParsers(final JSONObject formData) {
        Object values = formData.get(CONSOLE_LOG_PARSERS_KEY);
        Set<String> parsers = new HashSet<String>();
        if (values instanceof JSONArray) {
            JSONArray array = (JSONArray)values;
            for (int i = 0; i < array.size(); i++) {
                add(parsers, array.getJSONObject(i));
            }
            formData.remove(CONSOLE_LOG_PARSERS_KEY);
        }
        else if (values instanceof JSONObject) {
            add(parsers, (JSONObject)values);
            formData.remove(CONSOLE_LOG_PARSERS_KEY);
        }

        return parsers;
    }

    private boolean add(final Set<String> parsers, final JSONObject element) {
        return parsers.add(element.getString(PARSER_NAME_ATTRIBUTE));
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

        save();

        return true;
    }

    @Override
    public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
            @QueryParameter final String pattern) throws IOException {
        FormValidation required = FormValidation.validateRequired(pattern);
        if (required.kind == FormValidation.Kind.OK) {
            return FilePath.validateFileMask(project.getSomeWorkspace(), pattern);
        }
        else {
            return required;
        }
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
            boolean hasMultiLineSupport = regexp.contains("\\n");

            return parseExample(script, example, regexp, hasMultiLineSupport);
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
}