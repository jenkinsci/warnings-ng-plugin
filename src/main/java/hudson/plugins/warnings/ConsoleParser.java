package hudson.plugins.warnings;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.warnings.parser.ParserDescription;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.util.ListBoxModel;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.Lists;

/**
 * Defines the configuration to parse a set of files using a predefined parser.
 *
 * @author Ulli Hafner
 */
public class ConsoleParser extends AbstractDescribableImpl<ConsoleParser> {
    private final String parserName;

    /**
     * Creates a new instance of {@link ConsoleParser}.
     *
     * @param parserName
     *            the name of the parser to use
     */
    @DataBoundConstructor
    public ConsoleParser(final String parserName) {
        super();

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
     * Removes non-existing parsers from the specified list.
     *
     * @param parsers
     *            the parsers
     * @return a new list containing the filtered parsers
     */
    public static ConsoleParser[] filterExisting(final Collection<? extends ConsoleParser> parsers) {
        List<ConsoleParser> existing = Lists.newArrayList();
        for (ConsoleParser parser : parsers) {
            if (ParserRegistry.exists(parser.getParserName())) {
                existing.add(parser);
            }
        }
        return existing.toArray(new ConsoleParser[existing.size()]);
    }

    /**
     * Dummy descriptor for {@link ConsoleParser}.
     *
     * @author Ulli Hafner
     */
   @Extension
   public static class DescriptorImpl extends Descriptor<ConsoleParser> {
       /**
        * Returns the available parsers. These values will be shown in the list
        * box of the config.jelly view part.
        *
        * @return the model of the list box
        */
       public ListBoxModel doFillParserNameItems() {
           ListBoxModel items = new ListBoxModel();
           for (ParserDescription parser : ParserRegistry.getAvailableParsers()) {
               items.add(parser.getGroup());
           }
           return items;
       }

       @Override
       public String getDisplayName() {
           return StringUtils.EMPTY;
       }
   }

}