package hudson.plugins.warnings.parser;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import hudson.Extension;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for maven console warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class MavenConsoleParser extends RegexpLineParser {
    private static final String WARNING = "WARNING";
    private static final String ERROR = "ERROR";

    private static final long serialVersionUID = 1737791073711198075L;

    private static final String PATTERN = "^.*\\[(WARNING|ERROR)\\]\\s*(.*)$";

    /**
     * Creates a new instance of {@link MavenConsoleParser}.
     */
    public MavenConsoleParser() {
        super(Messages._Warnings_Maven_ParserName(),
                Messages._Warnings_Maven_LinkName(),
                Messages._Warnings_Maven_TrendName(),
                PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains(WARNING) || line.contains(ERROR);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        String category;
        if (ERROR.equals(matcher.group(1))) {
            priority = Priority.HIGH;
            category = "Error";
        }
        else {
            priority = Priority.NORMAL;
            category = "Warning";
        }
        return createWarning("Console Log", getCurrentLine(), category, matcher.group(2), priority);
    }

    @Override
    protected Collection<FileAnnotation> postProcessWarnings(final List<FileAnnotation> warnings) {
        List<FileAnnotation> condensed = Lists.newArrayList();
        int line = -1;
        for (FileAnnotation warning : warnings) {
            if (warning.getPrimaryLineNumber() == line + 1 && !condensed.isEmpty()) {
                FileAnnotation previous = condensed.get(condensed.size() - 1);
                if (previous.getPriority() == warning.getPriority()) {
                    condensed.remove(condensed.size() - 1);
                    condensed.add(new Warning(previous, warning.getMessage(), warning.getPrimaryLineNumber()));
                }
            }
            else {
                condensed.add(warning);
            }
            line = warning.getPrimaryLineNumber();
        }
        List<FileAnnotation> noBlank = Lists.newArrayList();
        for (FileAnnotation warning : condensed) {
            if (StringUtils.isNotBlank(warning.getMessage())) {
                noBlank.add(warning);
            }
        }
        return noBlank;
    }
}

