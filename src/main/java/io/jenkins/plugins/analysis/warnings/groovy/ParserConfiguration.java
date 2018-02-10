package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.util.NoSuchElementException;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory.StaticAnalysisToolFactory;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import hudson.Extension;
import hudson.util.ListBoxModel;

/**
 * Global configuration of Groovy based parsers. These parsers are dynamically registered.
 *
 * @author Ullrich Hafner
 */
@Extension
@Symbol("warningsParser") // FIXME: check name before going live
public class ParserConfiguration extends GlobalConfiguration {
    private ImmutableList<GroovyParser> parsers = Lists.immutable.empty();

    /**
     * Returns the singleton instance of this {@link ParserConfiguration}.
     *
     * @return the singleton instance
     */
    public static ParserConfiguration getInstance() {
        return GlobalConfiguration.all().get(ParserConfiguration.class);
    }

    /**
     * Returns the list of available Groovy parsers.
     *
     * @return the Groovy parsers
     */
    public List<GroovyParser> getParsers() {
        return parsers.castToList();
    }

    /**
     * Sets the list of available Groovy parsers to the specified elements. Previously set parsers will be removed.
     *
     * @param parsers
     *         the new Groovy parsers
     */
    @DataBoundSetter
    public void setParsers(final List<GroovyParser> parsers) {
        this.parsers = Lists.immutable.withAll(parsers);
    }

    /**
     * Returns whether the current user has the permission to edit the available Groovy parsers.
     *
     * @return {@code true} if the user has the right, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called from config.jelly
    public boolean canEditParsers() {
        return Jenkins.getInstance().getACL().hasPermission(Jenkins.RUN_SCRIPTS);
    }

    public StaticAnalysisTool getParser(final String id) {
        for (GroovyParser parser : parsers) {
            if (parser.getId().equals(id)) {
                return parser.toStaticAnalysisTool();
            }
        }
        throw new NoSuchElementException("No Groovy parser with ID '%s' found.", id);
    }

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
     * Registers all Groovy parsers as static analysis tools in the {@link LabelProviderFactory} of the analysis-core
     * plug-in so that these parsers can be referenced in steps and publishers.
     */
    @Extension
    @SuppressWarnings("unused") // Picked up by Jenkins Extension Scanner
    public static class ParserFactory implements StaticAnalysisToolFactory {
        @Override
        public List<StaticAnalysisTool> getTools() {
            return getInstance().parsers.stream()
                    .map(GroovyParser::toStaticAnalysisTool)
                    .collect(Collectors.toList());
        }
    }
}
