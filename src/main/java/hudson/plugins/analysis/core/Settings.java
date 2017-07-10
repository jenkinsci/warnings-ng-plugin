package hudson.plugins.analysis.core;

/**
 * Global settings for all static code analysis plug-ins.
 *
 * @author Ulli Hafner
 */
public interface Settings {
    /**
     * Returns whether the logger should be quite. If the logger is quite then the output is not shown in the
     * console log.
     *
     * @return on <code>true</code> no logging statements are written to the console log
     */
    Boolean getQuietMode();

    /**
     * Returns whether a build should be failed if the parsed input file is invalid or corrupted.
     *
     * @return on <code>true</code> the build will be failed, on <code>false</code> an error message is reported
     */
    Boolean getFailOnCorrupt();

    /**
     * Returns whether author and commit information should be omitted.
     *
     * @return on <code>true</code> the SCM will not be called to obtain author and commit information,
     * on <code>false</code> author and commit information are created
     */
    Boolean getNoAuthors();
}
