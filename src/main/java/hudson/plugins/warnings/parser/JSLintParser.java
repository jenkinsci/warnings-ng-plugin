package hudson.plugins.warnings.parser;

import hudson.Extension;

/**
 * A parser for JSLint checks warnings.
 *
 * @author Gavin Mogan
 */
@Extension
public class JSLintParser extends LintParser {
    private static final long serialVersionUID = 8613418992526753095L;

    /**
     * Creates a new instance of {@link JSLintParser}.
     */
    public JSLintParser() {
        super(Messages._Warnings_JSLint_ParserName(),
                Messages._Warnings_JSLint_LinkName(),
                Messages._Warnings_JSLint_TrendName());
    }
}
