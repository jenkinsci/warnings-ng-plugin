package io.jenkins.plugins.analysis.core.util;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import hudson.EnvVars;

/**
 * Unit test for {@link EnvironmentResolver}.
 *
 * @author Deniz Mardin
 * @author Frank Christian Geyer
 */
class EnvironmentResolverTest {

    private static final int NUMBER_OF_SIGNS = 1024;
    private static final String KEY = "$KEY";
    private static final String VALUE = "$VALUE";

    /**
     * Verifies that the output is empty for an empty input.
     */
    @Test
    void shouldReturnUnmodifiedOutputForEmptyInput() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final String expanded = environmentResolver.expandEnvironmentVariables(new EnvVars(), "");

        assertThat(expanded).isEmpty();
    }

    /**
     * Verifies that the output is the same for a conventional input.
     */
    @Test
    void shouldReturnSameOutputAsInput() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final String expanded = environmentResolver.expandEnvironmentVariables(new EnvVars(), "TestString");

        assertThat(expanded).isEqualTo("TestString");
    }

    /**
     * Verifies that the output is null for an input which is null.
     */
    @Test
    void shouldReturnNullForNullAsInput() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final String expanded = environmentResolver.expandEnvironmentVariables(new EnvVars(), null);

        assertThat(expanded).isNull();
    }

    /**
     * Verifies that the output is the same as the input for an environment which is set to null.
     */
    @Test
    void shouldReturnSameOutputAsInputForAnEnvironmentWhichIsNull() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final String expanded = environmentResolver.expandEnvironmentVariables(null, "TestStringForNull");

        assertThat(expanded).isEqualTo("TestStringForNull");
    }

    /**
     * Verifies that the output is null when both, the environment and the input are null.
     */
    @Test
    void shouldReturnNullWhenBothParametersEnvironmentAndTheInputAreNull() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final String expanded = environmentResolver.expandEnvironmentVariables(null, null);

        assertThat(expanded).isNull();
    }

    /**
     * Verifies that the output is an adjusted value of a given special input, which is based on $-signs.
     */
    @Test
    void shouldReturnAdjustedOutputForAGivenInputBasedOnDollarSigns() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final EnvVars envVars = new EnvVars();
        envVars.put(KEY, VALUE);
        final String previous = "$$Test$$Testing$TestString$$Testing$Test";
        final String expanded = environmentResolver.expandEnvironmentVariables(envVars, previous);
        final String expected = "$Test$Testing$TestString$Testing$Test";

        assertThat(expanded).isEqualTo(expected);
    }

    /**
     * Verifies that the output is the same for a given input without further adjustments.
     */
    @Test
    void shouldReturnNonAdjustedOutputForDefinedInput() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final EnvVars envVars = new EnvVars();
        envVars.put(KEY, VALUE);
        final String expected = "$Test$Testing";
        final String expanded = environmentResolver.expandEnvironmentVariables(envVars, expected);

        assertThat(expanded).isEqualTo(expected);
    }

    /**
     * Verifies that the loop gets exited after the statical set retries {@code RESOLVE_VARIABLES_DEPTH} successfully
     * and the second condition StringUtils.isNotBlank(expanded) is fulfilled.
     */
    @Test
    void shouldExitAfterSetRetriesHasExceededAndHenceLeaveTheInputUnmodified() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final EnvVars envVars = new EnvVars();
        envVars.put(KEY, VALUE);
        final String expanded = environmentResolver.expandEnvironmentVariables(envVars, createString('$'));

        assertThat(expanded).isEqualTo("$");
    }

    /**
     * Verifies that the loop gets exited after the statical set retries {@code RESOLVE_VARIABLES_DEPTH} successfully
     * and the second condition StringUtils.isNotBlank(expanded) is not fulfilled.
     */
    @Test
    void shouldExitAfterSetRetriesHasExceededAndHenceLeaveTheInputUnmodifiedTestTheSecondShortCircuitCondition() {
        final EnvironmentResolver environmentResolver = new EnvironmentResolver();
        final EnvVars envVars = new EnvVars();
        envVars.put(KEY, VALUE);
        final String expanded = environmentResolver.expandEnvironmentVariables(envVars, createString(' '));

        assertThat(expanded).isEqualTo(expanded);
    }

    private String createString(final char character) {
        final char[] chars = new char[NUMBER_OF_SIGNS];
        Arrays.fill(chars, character);
        return new String(chars);
    }

}