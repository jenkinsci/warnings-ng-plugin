package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the Reshaper InspectCode compiler warnings.
 *
 * @author Rafal Jasica
 */
@Extension
public class ReshaperInspectCodeParser extends RegexpLineParser {
    private static final long serialVersionUID = 526872513348892L;
    static final String WARNING_TYPE = "ReshaperInspectCode";
    private static final String WARNING_PATTERN = "<Issue.*?TypeId=\"(?<Type>.*?)\".*?File=\"(?<File>.*?)\".*?Line=\"(?<Line>.*?)\".*?Message=\"(?<Message>.*?)\"";

    /**
     * Creates a new instance of {@link ReshaperInspectCodeParser}.
     */
    public ReshaperInspectCodeParser() {
        this(Messages._Warnings_ReshaperInspectCode_ParserName(), 
                Messages._Warnings_ReshaperInspectCode_LinkName(),
                Messages._Warnings_ReshaperInspectCode_TrendName());
    }

    /**
     * Creates a new instance of {@link ReshaperInspectCodeParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    public ReshaperInspectCodeParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        super(parserName, linkName, trendName, WARNING_PATTERN);
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
}
