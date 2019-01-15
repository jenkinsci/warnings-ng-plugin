package edu.hm.hafner.util;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Parses integers from string values.
 *
 * @author Ullrich Hafner
 */
public final class IntegerParser {
    /**
     * Converts a number (represented by the specified String) to an integer value. If the string is not a valid number,
     * then 0 is returned. This method does not throw exceptions if the value is invalid.
     *
     * @param number
     *         the line number (as a string)
     *
     * @return the converted number
     * @see Integer#parseInt(String)
     */
    public static int parseInt(@Nullable final String number) {
        if (StringUtils.isNotBlank(number)) {
            try {
                return Integer.parseInt(number);
            }
            catch (NumberFormatException ignored) {
                // ignore and return 0
            }
        }
        return 0;
    }

    private IntegerParser() {
        // prevents instantiation
    }
}
