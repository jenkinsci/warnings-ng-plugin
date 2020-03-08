package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.ThresholdValidation;

/**
 * Tests the class {@link ThresholdValidation}.
 *
 * @author Ullrich Hafner
 */
class DryThresholdValidationTest {
    @ParameterizedTest(name = "{index} => high={0}, normal={1}")
    @CsvSource({
            "25, 50",
            "49, 50",
            " 1,  2"})
    @DisplayName("OK: high > normal")
    void shouldValidateSuccessfullyIfHighIsLargerThanNormal(final int normal, final int high) {
        ThresholdValidation validation = new ThresholdValidation();

        assertThat(validation.validateNormal(high, normal)).isOk();
        assertThat(validation.validateHigh(high, normal)).isOk();
    }

    @ParameterizedTest(name = "{index} => high={0}, normal={1}")
    @CsvSource({
            "50, 50",
            "51, 50",
            "500, 50",
            "-1, 50"})
    @DisplayName("ERROR: high <= normal")
    void shouldReportErrorIfHighIsLessOrEqualThanNormal(final int normal, final int high) {
        ThresholdValidation validation = new ThresholdValidation();

        assertThat(validation.validateNormal(high, normal)).isError();
        assertThat(validation.validateHigh(high, normal)).isError();
    }

    /**
     * Verifies that the getters return the default values for illegal thresholds.
     */
    @Test
    void shouldReturnCorrectValuesEvenOnInvalidInput() {
        ThresholdValidation validation = new ThresholdValidation();

        assertThat(validation.getNormalThreshold(1, 2)).isEqualTo(1);
        assertThat(validation.getNormalThreshold(1, 20)).isEqualTo(1);
        assertThat(validation.getNormalThreshold(19, 20)).isEqualTo(19);

        assertThat(validation.getNormalThreshold(10, 10))
                .isEqualTo(ThresholdValidation.DEFAULT_NORMAL_THRESHOLD);
        assertThat(validation.getNormalThreshold(0, 10))
                .isEqualTo(ThresholdValidation.DEFAULT_NORMAL_THRESHOLD);
        assertThat(validation.getNormalThreshold(-1, 10))
                .isEqualTo(ThresholdValidation.DEFAULT_NORMAL_THRESHOLD);
        assertThat(validation.getNormalThreshold(5, 4))
                .isEqualTo(ThresholdValidation.DEFAULT_NORMAL_THRESHOLD);

        assertThat(validation.getHighThreshold(1, 2)).isEqualTo(2);
        assertThat(validation.getHighThreshold(1, 20)).isEqualTo(20);
        assertThat(validation.getHighThreshold(19, 20)).isEqualTo(20);

        assertThat(validation.getHighThreshold(10, 10))
                .isEqualTo(ThresholdValidation.DEFAULT_HIGH_THRESHOLD);
        assertThat(validation.getHighThreshold(0, 10))
                .isEqualTo(ThresholdValidation.DEFAULT_HIGH_THRESHOLD);
        assertThat(validation.getHighThreshold(-1, 10))
                .isEqualTo(ThresholdValidation.DEFAULT_HIGH_THRESHOLD);
        assertThat(validation.getHighThreshold(5, 4))
                .isEqualTo(ThresholdValidation.DEFAULT_HIGH_THRESHOLD);
    }
}

