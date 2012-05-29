package hudson.plugins.warnings;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

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

    /**
     * Dummy descriptor for {@link ParserConfiguration}.
     *
     * @author Ulli Hafner
     */
   @Extension
   public static class DescriptorImpl extends Descriptor<ParserConfiguration> {
       @Override
       public String getDisplayName() {
           return StringUtils.EMPTY;
       }
   }
}

