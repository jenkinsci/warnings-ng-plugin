package hudson.plugins.warnings.parser;

import hudson.Extension;

import java.util.regex.Matcher;

/**
 * A parser for the Clang compiler warnings.
 *
 * @author Neil Davis
 */
@Extension
public class AppleLLVMClangParser extends RegexpLineParser {
    private static final long serialVersionUID = -3015592762345283182L;
    private static final String CLANG_WARNING_PATTERN = "^\\s*(?:\\d+%)?([^%]*?):(\\d+):(?:\\d+:)?(?:(?:\\{\\d+:\\d+-\\d+:\\d+\\})+:)?\\s*warning:\\s*(.*?)(?:\\[(.*)\\])?$";

    /**
     * Creates a new instance of {@link AppleLLVMClangParser}.
     */
    public AppleLLVMClangParser() {
        super(Messages._Warnings_AppleLLVMClang_ParserName(),
                Messages._Warnings_AppleLLVMClang_LinkName(),
                Messages._Warnings_AppleLLVMClang_TrendName(),
                CLANG_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String filename = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String message = matcher.group(3);
        String category = matcher.group(4);

        if (category == null) {
            return createWarning(filename, lineNumber, message);
        }

        return createWarning(filename, lineNumber, category, message);
    }

    @Override
    protected String getId() {
        return "Apple LLVM Compiler (Clang)"; // old ID in serialization
    }
}
