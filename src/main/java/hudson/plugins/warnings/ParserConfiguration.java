package hudson.plugins.warnings;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.google.common.collect.Lists;

/**
 * Defines the configuration to parse a set of files using a predefined parser.
 *
 * @author Ulli Hafner
 */
public class ParserConfiguration extends AbstractDescribableImpl<ParserConfiguration> {
    private final String pattern;
    private final String parserName;

    /**
     * Creates a new instance of {@link ParserConfiguration}.
     *
     * @param pattern
     *            the pattern of files to parse
     * @param parserName
     *            the name of the parser to use
     */
    @DataBoundConstructor
    public ParserConfiguration(final String pattern, final String parserName) {
        super();

        this.pattern = pattern;
        this.parserName = parserName;
    }

    /**
     * Returns the name of the parser.
     *
     * @return the parser name
     */
    public String getParserName() {
        return parserName;
    }

    /**
     * Returns the file pattern.
     *
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", parserName, pattern);
    }

    /**
     * Removes non-existing parsers from the specified collection of parsers.
     *
     * @param parsers
     *            the parsers
     * @return a new list containing the filtered parsers
     */
    public static ParserConfiguration[] filterExisting(final List<? extends ParserConfiguration> parsers) {
        List<ParserConfiguration> existing = Lists.newArrayList();
        for (ParserConfiguration parser : parsers) {
            if (ParserRegistry.exists(parser.getParserName())) {
                existing.add(parser);
            }
        }
        return existing.toArray(new ParserConfiguration[existing.size()]);
    }

    /**
     * Dummy descriptor for {@link ParserConfiguration}.
     *
     * @author Ulli Hafner
     */
   @Extension
   public static class DescriptorImpl extends Descriptor<ParserConfiguration> {
        /**
         * Returns the available parsers. These values will be shown in the list
         * box of the config.jelly view part.
         *
         * @return the model of the list box
         */
        public ListBoxModel doFillParserNameItems() {
            return ParserRegistry.getParsersAsListModel();
        }

       @Override
       public String getDisplayName() {
           return StringUtils.EMPTY;
       }

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
   }
}

