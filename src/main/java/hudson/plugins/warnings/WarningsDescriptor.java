package hudson.plugins.warnings;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.NullBuildHistory;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.graph.DefaultGraphConfigurationView;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Descriptor for the class {@link WarningsPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
@Extension(ordinal = 100) // NOCHECKSTYLE
public final class WarningsDescriptor extends PluginDescriptor implements StaplerProxy {
    private static final String CONSOLE_LOG_PARSERS_KEY = "consoleLogParsers";
    private static final String FILE_LOCATIONS_KEY = "locations";
    private static final String PARSER_NAME_ATTRIBUTE = "parserName";

    /** The ID of this plug-in is used as URL. */
    static final String PLUGIN_ID = "warnings";
    /** The URL of the result action. */
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);
    /** Prefix of icons in this plug-in. */
    public static final String IMAGE_PREFIX = "/plugin/warnings/icons/";
    /** Icon to use for the sidebar links. */
    public static final String SMALL_ICON_URL = IMAGE_PREFIX + "warnings-24x24.png";
    /** Icon to use for the result summary. */
    public static final String LARGE_ICON_URL = IMAGE_PREFIX + "warnings-48x48.png";

    private final CopyOnWriteList<GroovyParser> groovyParsers = new CopyOnWriteList<GroovyParser>();

    /**
     * Returns the URL of the warning results for the specified parser.
     *
     * @param group
     *            the parser group
     * @return a unique URL
     */
    public static String getResultUrl(final String group) {
        if (group == null) { // prior 4.0
            return RESULT_URL;
        }
        else {
            return PLUGIN_ID + ParserRegistry.getUrl(group) + RESULT_URL_SUFFIX;
        }
    }

    /**
     * Returns the URL of the warning project for the specified parser.
     *
     * @param group
     *            the parser group
     * @return a unique URL
     */
    public static String getProjectUrl(final String group) {
        if (group == null) { // prior 4.0
            return PLUGIN_ID;
        }
        else {
            return PLUGIN_ID + ParserRegistry.getUrl(group);
        }
    }

    /**
     * Returns the graph configuration screen.
     *
     * @param link
     *            the link to check
     * @param request
     *            stapler request
     * @param response
     *            stapler response
     * @return the graph configuration or <code>null</code>
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if ("configureDefaults".equals(link)) {
            Ancestor ancestor = request.findAncestor(AbstractProject.class);
            if (ancestor.getObject() instanceof AbstractProject) {
                AbstractProject<?, ?> project = (AbstractProject<?, ?>)ancestor.getObject();
                return new DefaultGraphConfigurationView(
                        new GraphConfiguration(WarningsProjectAction.getAllGraphs()), project, "warnings",
                        new NullBuildHistory(),
                        project.getAbsoluteUrl() + "/descriptorByName/WarningsPublisher/configureDefaults/");
            }
        }
        return null;
    }

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

    @Override
    public String getDisplayName() {
        return Messages.Warnings_Publisher_Name();
    }

    @Override
    public String getPluginName() {
        return PLUGIN_ID;
    }

    @Override
    public String getIconUrl() {
        return SMALL_ICON_URL;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }

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
    public GroovyParser[] getParsers() {
        return groovyParsers.toArray(new GroovyParser[groovyParsers.size()]);
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

    /** {@inheritDoc} */
    public Object getTarget() {
        return this;
    }
}