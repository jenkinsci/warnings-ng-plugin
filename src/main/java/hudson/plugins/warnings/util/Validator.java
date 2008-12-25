package hudson.plugins.warnings.util;

import hudson.util.FormFieldValidator;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Checks an input string. Dummy interface to simplify testing of
 * {@link FormFieldValidator} classes.
 *
 * @author Ulli Hafner
 */
public interface Validator {
    /**
     * Check the input.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     */
    void check() throws IOException, ServletException;
}
