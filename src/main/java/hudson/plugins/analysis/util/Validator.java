package hudson.plugins.analysis.util;

import hudson.util.FormValidation;

/**
 * Validator for string values.
 *
 * @author Ulli Hafner
 */
public interface Validator {
    /**
     * Validates the specified value. If the value is not valid then a
     * {@link FormValidation} exception must be thrown.
     *
     * @param value the value to validate
     * @return a positive {@link FormValidation} object
     */
    FormValidation check(final String value) throws FormValidation;
}

