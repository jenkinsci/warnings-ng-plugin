package io.jenkins.plugins.analysis.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.util.FormValidation;

/**
 * Custom assertions for {@link FormValidation} instances.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("NM")
public class Assertions extends edu.hm.hafner.analysis.assertj.Assertions {
    /**
     * An entry point for {@link FormValidationAssert} to follow AssertJ standard {@code assertThat()}. With a static
     * import, one can write directly {@code assertThat(myIssues)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link FormValidationAssert}
     */
    public static FormValidationAssert assertThat(final FormValidation actual) {
        return new FormValidationAssert(actual);
    }
}
