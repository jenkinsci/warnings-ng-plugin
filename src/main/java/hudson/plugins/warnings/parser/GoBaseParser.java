package hudson.plugins.warnings.parser;

import org.jvnet.localizer.Localizable;

import hudson.plugins.warnings.WarningsDescriptor;

/**
 * Base class for go vet / golint parsers
 *
 * @author Ryan Cox
 * @deprecated use the new analysis-model library
 */
@Deprecated
abstract class GoBaseParser extends RegexpLineParser {


    static final String GO_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "go-24x24.png";
    static final String GO_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "go-48x48.png";


    /**
     * Constructor to pass through args to RegexpLineParser
     */
    public GoBaseParser(final Localizable parserName, final Localizable linkName, final Localizable trendName,
            final String warningPattern, final boolean isStringMatchActivated) {
        super(parserName,linkName, trendName, warningPattern, isStringMatchActivated);
    }

    @Override
    public String getSmallImage() {
        return GO_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return GO_LARGE_ICON;
    }
}
