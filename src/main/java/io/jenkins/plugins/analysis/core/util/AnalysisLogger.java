package io.jenkins.plugins.analysis.core.util;

import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;

/**
 * A simple logger that prefixes each message with the static analysis tool.
 *
 * @author Ulli Hafner
 */
class AnalysisLogger implements Logger {
    private final String toolName;
    private final PrintStream delegate;

    /**
     * Creates a new instance of {@link AnalysisLogger}.
     *
     * @param logger
     *         the actual print stream to log to
     * @param toolName
     *         the plug-in name
     */
    AnalysisLogger(final PrintStream logger, final String toolName) {
        if (toolName.contains("[")) {
            this.toolName = toolName;
        }
        else {
            this.toolName = String.format("[%s] ", toolName);
        }
        delegate = logger;
    }

    @Override
    public void log(final String format, final Object... args) {
        delegate.println(StringUtils.defaultString(toolName) + String.format(format, args));
    }
}

