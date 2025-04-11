package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildableItem;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.FormValidation;
import jenkins.security.MasterToSlaveCallable;

import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.LogHandler;
import io.jenkins.plugins.util.ValidationUtilities;

/**
 * A tool that can produce a {@link Report report of issues} in some way. If your tool produces issues by scanning a
 * compiler log or static analysis report file, consider deriving from {@link AnalysisModelParser}.
 *
 * @author Ullrich Hafner
 * @see AnalysisModelParser
 */
public abstract class Tool extends AbstractDescribableImpl<Tool> implements Serializable {
    @Serial
    private static final long serialVersionUID = 3305739700153168629L;
    private static final ValidationUtilities VALIDATION_UTILITIES = new ValidationUtilities();

    private String id = StringUtils.EMPTY;
    private String name = StringUtils.EMPTY;
    private String icon = StringUtils.EMPTY; // @since 12.0.0: by default no custom icon is set

    private JenkinsFacade jenkins = new JenkinsFacade();

    @VisibleForTesting
    public void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkins = jenkinsFacade;
    }

    /**
     * Called after deserialization to retain backward compatibility.
     *
     * @return this
     */
    protected Object readResolve() {
        jenkins = new JenkinsFacade();

        if (icon == null) {
            icon = StringUtils.EMPTY;
        }

        return this;
    }

    /**
     * Overrides the default ID of the results. The ID is used as URL of the results and as identifier in UI elements.
     * If no ID is given, then the default ID is used, see corresponding {@link ToolDescriptor}.
     *
     * @param id
     *         the ID of the results
     *
     * @see ToolDescriptor#getId()
     */
    @DataBoundSetter
    public void setId(final String id) {
        VALIDATION_UTILITIES.ensureValidId(id);

        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the actual ID of the tool. If no user defined ID is given, then the default ID is returned.
     *
     * @return the ID
     * @see #setId(String)
     * @see ToolDescriptor#getId()
     */
    public String getActualId() {
        return StringUtils.defaultIfBlank(getId(), getDescriptor().getId());
    }

    /**
     * Overrides the name of the results. The name is used for all labels in the UI. If no name is given, then the
     * default name is used, see corresponding {@link ToolDescriptor}.
     *
     * @param name
     *         the name of the results
     *
     * @see ToolDescriptor#getName()
     */
    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the actual name of the tool. If no user defined name is given, then the default name is returned.
     *
     * @return the name
     * @see #setName(String)
     * @see ToolDescriptor#getName()
     */
    public String getActualName() {
        return StringUtils.defaultIfBlank(getName(), getDescriptor().getDisplayName());
    }

    /**
     * Defines the custom icon of the tool. If no icon is given, then the default icon of the tool is used.
     *
     * @param icon
     *         the icon of the tool
     */
    @DataBoundSetter
    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Returns the {@link Symbol} name of this tool.
     *
     * @return the name of this tool, or "undefined" if no symbol has been defined
     */
    public String getSymbolName() {
        return getDescriptor().getSymbolName();
    }

    /**
     * Returns the associated label provider for this tool.
     *
     * @return the label provider
     */
    public StaticAnalysisLabelProvider getLabelProvider() {
        StaticAnalysisLabelProvider labelProvider = getDescriptor().getLabelProvider();
        if (StringUtils.isNotBlank(name)) {
            labelProvider.setName(name);
        }
        return labelProvider;
    }

    @Override
    public ToolDescriptor getDescriptor() {
        return (ToolDescriptor) jenkins.getDescriptorOrDie(getClass());
    }

    /**
     * Scans the results of a build for issues. This method is invoked on Jenkins master. I.e., if a tool wants to
     * process some build results, it is required to run a {@link MasterToSlaveCallable}.
     *
     * @param run
     *         the build
     * @param workspace
     *         the workspace of the build
     * @param sourceCodeEncoding
     *         the encoding to use to read source files
     * @param logger
     *         the logger
     *
     * @return the created report
     * @throws ParsingException
     *         signals that during parsing a non-recoverable error has been occurred
     * @throws ParsingCanceledException
     *         signals that the user has aborted the parsing
     */
    public abstract Report scan(Run<?, ?> run, FilePath workspace, Charset sourceCodeEncoding, LogHandler logger)
            throws ParsingException, ParsingCanceledException;

    /** Descriptor for {@link Tool}. **/
    public abstract static class ToolDescriptor extends Descriptor<Tool> {
        private final String defaultId;

        /**
         * Creates a new instance of {@link ToolDescriptor} with the given ID.
         *
         * @param defaultId
         *         the unique ID of the tool
         */
        protected ToolDescriptor(final String defaultId) {
            super();

            VALIDATION_UTILITIES.ensureValidId(defaultId);
            this.defaultId = defaultId;
        }

        /**
         * Performs on-the-fly validation of the ID.
         *
         * @param project
         *         the project that is configured
         * @param id
         *         the ID of the tool
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckId(@AncestorInPath final BuildableItem project,
                @QueryParameter final String id) {
            if (!new JenkinsFacade().hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }

            return VALIDATION_UTILITIES.validateId(id);
        }

        @Override
        public String getId() {
            return defaultId;
        }

        /**
         * Returns the default name of this tool.
         *
         * @return the name
         */
        public String getName() {
            return getDisplayName();
        }

        /**
         * Returns the {@link Symbol} name of this tool.
         *
         * @return the name of this tool, or "undefined" if no symbol has been defined
         */
        public String getSymbolName() {
            Symbol annotation = getClass().getAnnotation(Symbol.class);

            if (annotation != null) {
                String[] symbols = annotation.value();
                if (symbols.length > 0) {
                    return symbols[0];
                }
            }
            return "unknownSymbol";
        }

        /**
         * Returns a {@link StaticAnalysisLabelProvider} that will render all tool specific labels.
         *
         * @return a tool specific {@link StaticAnalysisLabelProvider}
         */
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new StaticAnalysisLabelProvider(getId(), getDisplayName());
        }

        public String getIcon() {
            return getLabelProvider().getSmallIconUrl();
        }

        /**
         * Returns an optional help text that can provide useful hints on how to configure the static analysis tool so
         * that the report files could be parsed by Jenkins. This help can be a plain text message or an HTML snippet.
         *
         * @return the help
         */
        public String getHelp() {
            return StringUtils.EMPTY;
        }

        /**
         * Returns an optional URL to the homepage of the static analysis tool.
         *
         * @return the help
         */
        public String getUrl() {
            return StringUtils.EMPTY;
        }

        /**
         * Returns whether post-processing on the agent is enabled for this tool. If enabled, for all issues absolute
         * paths, fingerprints, packages and modules will be detected. Additionally, all affected files will be saved in
         * the build so that these files can be shown in the UI later on.,
         *
         * @return {@code true} if post-processing is enabled, {@code false} otherwise
         */
        public boolean isPostProcessingEnabled() {
            return true;
        }
    }
}
