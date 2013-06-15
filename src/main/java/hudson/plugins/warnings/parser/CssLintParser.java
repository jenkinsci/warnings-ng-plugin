package hudson.plugins.warnings.parser;

import hudson.Extension;

/**
 * A parser for CSS-Lint checks warnings.
 *
 * @author Ulli Hafner
 */
@Extension
public class CssLintParser extends LintParser {
    private static final long serialVersionUID = 8613418992526753095L;

    /**
     * Creates a new instance of {@link CssLintParser}.
     */
    public CssLintParser() {
        super(Messages._Warnings_CssLint_ParserName(),
                Messages._Warnings_CssLint_LinkName(),
                Messages._Warnings_CssLint_TrendName());
    }
}
