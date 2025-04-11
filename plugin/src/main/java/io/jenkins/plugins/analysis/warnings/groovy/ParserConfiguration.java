package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory.StaticAnalysisToolFactory;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.util.GlobalConfigurationFacade;
import io.jenkins.plugins.util.GlobalConfigurationItem;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Global configuration of Groovy based parsers. These parsers are dynamically registered.
 *
 * @author Ullrich Hafner
 */
@Extension
@Symbol("warningsParsers")
public class ParserConfiguration extends GlobalConfigurationItem {
    private List<GroovyParser> parsers = new ArrayList<>();
    private boolean consoleLogScanningPermitted;

    /**
     * Creates the Groovy parser configuration for the warnings plugins.
     */
    public ParserConfiguration() {
        super();

        load();
    }

    @VisibleForTesting
    ParserConfiguration(final GlobalConfigurationFacade facade) {
        super(facade);

        load();
    }

    /**
     * Returns the singleton instance of this {@link ParserConfiguration}.
     *
     * @return the singleton instance
     */
    public static ParserConfiguration getInstance() {
        return all().get(ParserConfiguration.class);
    }

    @Override
    protected void clearRepeatableProperties() {
        setParsers(new ArrayList<>());
    }

    /**
     * Returns the list of available Groovy parsers.
     *
     * @return the Groovy parsers
     */
    public List<GroovyParser> getParsers() {
        return parsers;
    }

    /**
     * Sets the list of available Groovy parsers to the specified elements. Previously set parsers will be removed.
     *
     * @param parsers
     *         the new Groovy parsers
     */
    @DataBoundSetter
    public void setParsers(final List<GroovyParser> parsers) {
        this.parsers = new ArrayList<>(parsers);

        save();
    }

    /**
     * Removes a GroovyParser from the list by ID.
     *
     * @param parserId
     *         the ID of the Groovy parser to be deleted
     */
    public void deleteParser(final String parserId) {
        if (contains(parserId)) {
            GroovyParser parser = getParser(parserId);
            this.parsers.remove(parser);
        }
        else {
            throw new NoSuchElementException(String.format("No Groovy parser with ID '%s' found.", parserId));
        }
        save();
    }

    /**
     * Adds a GroovyParser to the list without removing other parsers.
     *
     * @param parser
     *         the new Groovy parser to be added
     */
    public void addParser(final GroovyParser parser) {
        String parserId = parser.getId();
        if (contains(parserId)) {
            throw new IllegalArgumentException(String.format("ID '%s' already exists.", parserId));
        }
        this.parsers.add(parser);

        save();
    }

    /**
     * Says if the admin has permitted groovy parsers to scan a build's console log.
     *
     * @return true if groovy parsers can scan the console, false if they are
     *         limited to only scanning files on the build node.
     */
    public boolean isConsoleLogScanningPermitted() {
        return consoleLogScanningPermitted;
    }

    /**
     * Sets whether or not the admin has permitted groovy parsers to scan a build's
     * console log.
     *
     * @param consoleLogScanningPermitted true if groovy parsers can scan the
     *                                    console, false if they are limited to only
     *                                    scanning files on the build node.
     */
    @DataBoundSetter
    public void setConsoleLogScanningPermitted(final boolean consoleLogScanningPermitted) {
        this.consoleLogScanningPermitted = consoleLogScanningPermitted;

        save();
    }

    /**
     * Called by jelly to validate the configured value that could be passed to
     * {@link #setConsoleLogScanningPermitted(boolean)}.
     *
     * @param value The current value.
     * @return {@link FormValidation#ok()} if all is well, else a warning or error.
     */
    // Maintenance note: This is NOT annotated with @POST because it does not leak secrets.
    // Similarly, there's no need to permission-check either.
    // If it is ever changed to do anything non-trivial that might leak secrets then
    // it must be changed to @POST and checkMethod="post" be added to the checkbox.
    // There is a matching entry in archunit_ignore_patterns.txt to permit this.
    public FormValidation doCheckConsoleLogScanningPermitted(@QueryParameter final boolean value) {
        if (value) {
            return FormValidation.warning(Messages.ParserConfiguration_consoleLogScanningPermitted());
        }
        return FormValidation.ok();
    }

    /**
     * Returns whether the current user has the permission to edit the available Groovy parsers.
     *
     * @return {@code true} if the user has the right, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called from config.jelly
    public boolean canEditParsers() {
        return new JenkinsFacade().hasPermission(Jenkins.ADMINISTER);
    }

    /**
     * Returns the parser (wrapped into a {@link AnalysisModelParser} instance) with the specified ID.
     *
     * @param id
     *         the ID of the parser
     *
     * @return the parser
     * @throws NoSuchElementException
     *         if there is no such parser with the given ID
     */
    public GroovyParser getParser(final String id) {
        for (GroovyParser parser : parsers) {
            if (parser.getId().equals(id)) {
                return parser;
            }
        }
        throw new NoSuchElementException(String.format("No Groovy parser with ID '%s' found.", id));
    }

    /**
     * Returns whether a parser with the specified ID does already exist. Parser IDs are unique in a Jenkins instance.
     *
     * @param id
     *         the ID of the parser
     *
     * @return {@code true} if there is already a parser with the ID, {@code false} otherwise
     */
    public boolean contains(final String id) {
        for (GroovyParser parser : parsers) {
            if (parser.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all registered Groovy parsers. These are packed into a {@link ListBoxModel} in order to show them in the
     * list box of the config.jelly view part.
     *
     * @return the model of the list box
     */
    public ListBoxModel asListBoxModel() {
        ListBoxModel items = new ListBoxModel();
        for (GroovyParser parser : parsers) {
            items.add(parser.getName(), parser.getId());
        }
        return items;
    }

    /**
     * Registers all Groovy parsers as static analysis tools in the {@link LabelProviderFactory} so that these parsers
     * can be referenced in actions.
     */
    @Extension
    @SuppressWarnings("unused") // Picked up by Jenkins Extension Scanner
    public static class ParserFactory implements StaticAnalysisToolFactory {
        @Override
        public Optional<StaticAnalysisLabelProvider> getLabelProvider(final String id) {
            return getInstance().parsers.stream()
                    .filter(p -> id.equals(p.getId()))
                    .findAny()
                    .map(p -> new StaticAnalysisLabelProvider(p.getId(), p.getName()));
        }
    }
}
