package hudson.plugins.analysis.util;

import org.apache.commons.lang.StringUtils;

/**
 * Null object that always returns the empty string as module name.
 *
 * @author Ulli Hafner
 */
public class NullModuleDetector extends ModuleDetector {
    @Override
    public String guessModuleName(final String fileName) {
        return StringUtils.EMPTY;
    }
}

