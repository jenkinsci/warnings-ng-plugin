package io.jenkins.plugins.analysis.core.testutil;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.assertj.core.api.AbstractAssert;

import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

/**
 * Assertions for {@link FormValidation} instances.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "NonBooleanMethodNameMayNotStartWithQuestion", "PMD.LinguisticNaming"})
public class FormValidationAssert extends AbstractAssert<FormValidationAssert, FormValidation> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";

    /**
     * Creates a new {@link FormValidationAssert} to make assertions on an actual {@link FormValidation}.
     *
     * @param actual
     *         the {@link FormValidation} we want to make assertions on
     */
    public FormValidationAssert(final FormValidation actual) {
        super(actual, FormValidationAssert.class);
    }

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

    /**
     * Verifies that the kind of the {@link FormValidation} is {@link Kind#ERROR}.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the kind of the {@link FormValidation} is not {@link Kind#ERROR}.
     */
    public FormValidationAssert isError() {
        isNotNull();

        if (!Objects.equals(actual.kind, Kind.ERROR)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "kind", actual, "ERROR", "not an ERROR");
        }

        return this;
    }

    /**
     * Verifies that the kind of the {@link FormValidation} is {@link Kind#OK}.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the kind of the {@link FormValidation} is not {@link Kind#OK}.
     */
    public FormValidationAssert isOk() {
        isNotNull();

        if (!Objects.equals(actual.kind, Kind.OK)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "kind", actual, "OK", "not OK");
        }

        return this;
    }

    /**
     * Verifies that the message of the {@link FormValidation} equals to the expected message.
     *
     * @param expectedMessage
     *         the expected message of the validation result
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the message of the {@link FormValidation} is not equal to the expected message
     */
    public FormValidationAssert hasMessage(final String expectedMessage) {
        isNotNull();

        String actualMessage = StringEscapeUtils.unescapeHtml4(actual.getMessage());
        if (!Objects.equals(actualMessage, expectedMessage)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "message", StringEscapeUtils.unescapeHtml4(actual.toString()), expectedMessage,
                    actualMessage);
        }

        return this;
    }

    /**
     * Verifies that the message of the {@link FormValidation} contains the expected message text.
     *
     * @param expectedMessagePart
     *         a part of the expected message of the validation result
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the message of the {@link FormValidation} contains not the expected message part
     */
    public FormValidationAssert hasMessageContaining(final String expectedMessagePart) {
        isNotNull();

        String actualMessage = StringEscapeUtils.unescapeHtml4(actual.getMessage());
        if (!StringUtils.contains(actualMessage, expectedMessagePart)) {
            failWithMessage("%nExpecting %s of:%n <%s>%nto contain:%n <%s>%nbut was:%n <%s>.", "message", StringEscapeUtils.unescapeHtml4(actual.toString()), expectedMessagePart,
                    actualMessage);
        }

        return this;

    }
}
