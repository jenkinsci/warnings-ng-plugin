package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.jvnet.localizer.Localizable;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.WarningsDescriptor;

/**
 * A parser for the Resharper InspectCode compiler warnings.
 *
 * @author Rafal Jasica
 */
@Extension
public class ResharperInspectCodeParser extends RegexpLineParser {
    private static final String RESHAPER_SMALL_ICON = WarningsDescriptor.IMAGE_PREFIX + "resharper-24x24.png";
    private static final String RESHAPER_LARGE_ICON = WarningsDescriptor.IMAGE_PREFIX + "resharper-48x48.png";

    private static final long serialVersionUID = 526872513348892L;
    private static final String WARNING_TYPE = "ResharperInspectCode";
    private static final String WARNING_PATTERN = "\\<Issue.*?TypeId=\"(.*?)\".*?File=\"(.*?)\".*?Line=\"(.*?)\".*?Message=\"(.*?)\"";

    /**
     * Creates a new instance of {@link ResharperInspectCodeParser}.
     */
    public ResharperInspectCodeParser() {
        this(Messages._Warnings_ReshaperInspectCode_ParserName(),
                Messages._Warnings_ReshaperInspectCode_LinkName(),
                Messages._Warnings_ReshaperInspectCode_TrendName());
    }

    /**
     * Creates a new instance of {@link ResharperInspectCodeParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public ResharperInspectCodeParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName, WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        return createWarning(
            matcher.group(2),
            getLineNumber(matcher.group(3)),
            WARNING_TYPE,
            matcher.group(1),
            matcher.group(4),
            Priority.NORMAL);
    }

    @Override
    protected String getId() {
        return "Reshaper InspectCode";
    }

    @Override
    public String getSmallImage() {
        return RESHAPER_SMALL_ICON;
    }

    @Override
    public String getLargeImage() {
        return RESHAPER_LARGE_ICON;
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("<Issue");
    }
}
