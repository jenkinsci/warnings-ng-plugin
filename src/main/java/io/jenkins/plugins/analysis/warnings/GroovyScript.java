package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.util.Ensure;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser;
import io.jenkins.plugins.analysis.warnings.groovy.ParserConfiguration;

import hudson.Extension;
import hudson.util.ListBoxModel;

/**
 * Selects a {@link GroovyParser} using the specified ID.
 *
 * @author Ullrich Hafner
 */
public class GroovyScript extends StaticAnalysisTool {
    private static final long serialVersionUID = 8580859196688994603L;
    static final String ID = "groovy";

    private String id;

    /**
     * Creates a new instance of {@link GroovyScript}.
     */
    @DataBoundConstructor
    public GroovyScript() {
        // empty constructor required for stapler
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the Groovy parser that should be used.
     *
     * @param id
     *         ID of the Groovy parser
     */
    @DataBoundSetter
    public void setId(final String id) {
        Ensure.that(ParserConfiguration.getInstance().contains(id)).isTrue(
                "There is no such parser with ID '%s'", id);

        this.id = id;
    }

    @Override
    public IssueParser createParser() {
        return getTool().createParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new StaticAnalysisLabelProvider(id, getTool().getName());
    }

    private StaticAnalysisTool getTool() {
        return ParserConfiguration.getInstance().getParser(id);
    }

    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Groovy_DescribableName();
        }

        /**
         * Returns all registered Groovy parsers. These are packed into a {@link ListBoxModel} in order to show them in
         * the list box of the config.jelly view part.
         *
         * @return the model of the list box
         */
        @SuppressWarnings("unused") // Called from config.jelly
        public ListBoxModel doFillIdItems() {
            ListBoxModel options = ParserConfiguration.getInstance().asListBoxModel();
            if (options.isEmpty()) {
                return options.add(Messages.Warnings_Groovy_NoParsersDefined());
            }
            return options;
        }
    }
}
