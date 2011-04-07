package hudson.plugins.analysis.util;

import org.apache.commons.lang.StringUtils;

/**
 * Null object that always returns the empty string as module name.
 *
 * @author Ulli Hafner
 */
public class NullModuleDetector extends ModuleDetector {
    /** {@inheritDoc} */
    @Override
    public String guessModuleName(final String fileName, final boolean isMavenBuild, final boolean isAntBuild) {
        return StringUtils.EMPTY;
    }

    /** {@inheritDoc} */
    @Override
    public String guessModuleName(final String originalFileName) {
        return StringUtils.EMPTY;
    }
}

