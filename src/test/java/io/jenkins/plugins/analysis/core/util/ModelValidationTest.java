package io.jenkins.plugins.analysis.core.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import edu.hm.hafner.analysis.Severity;

import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static io.jenkins.plugins.analysis.core.testutil.SoftAssertions.*;
import static io.jenkins.plugins.analysis.core.util.ModelValidation.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ModelValidation}.
 *
 * @author Arne Schöntag
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 */
class ModelValidationTest {
    @Test
    void shouldValidateCharsets() {
        ModelValidation model = new ModelValidation();

        assertSoftly(softly -> {
            softly.assertThat(model.validateCharset("")).isOk();
            softly.assertThat(model.validateCharset("UTF-8")).isOk();
            softly.assertThat(model.validateCharset("Some wrong text"))
                    .isError()
                    .hasMessage(createWrongEncodingErrorMessage());
        });
    }

    @Test
    void shouldContainDefaultCharsets() {
        ModelValidation model = new ModelValidation();

        ComboBoxModel allCharsets = model.getAllCharsets();
        assertThat(allCharsets).isNotEmpty().contains("UTF-8", "ISO-8859-1");
    }

    @Test
    void shouldFallbackToPlatformCharset() {
        ModelValidation model = new ModelValidation();
        
        assertThat(model.getCharset("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(model.getCharset("nothing")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset("")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset(null)).isEqualTo(Charset.defaultCharset());
    }

    @Test
    void doFillMinimumPriorityItemsShouldBeNotEmpty() {
        ModelValidation model = new ModelValidation();
        ListBoxModel allFilters = model.getAllSeverityFilters();

        assertThat(allFilters.size()).isEqualTo(3);

        Option actualHighOption = allFilters.get(0);
        Option actualNormalOption = allFilters.get(1);
        Option actualLowOption = allFilters.get(2);

        assertSoftly(softly -> {
            softly.assertThat(actualHighOption.value).isEqualTo(Severity.WARNING_HIGH.getName());
            softly.assertThat(actualHighOption.name).isEqualTo(Messages.SeverityFilter_High());
            softly.assertThat(actualNormalOption.value).isEqualTo(Severity.WARNING_NORMAL.getName());
            softly.assertThat(actualNormalOption.name).isEqualTo(Messages.SeverityFilter_Normal());
            softly.assertThat(actualLowOption.value).isEqualTo(Severity.WARNING_LOW.getName());
            softly.assertThat(actualLowOption.name).isEqualTo(Messages.SeverityFilter_Low());
        });
    }

    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getAllJobs()).thenReturn(new HashSet<>());

        ModelValidation model = new ModelValidation(jenkins);

        assertThat(model.getAllJobs()).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        String jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));
        ModelValidation model = new ModelValidation(jenkins);

        assertSoftly(softly -> {
            softly.assertThat(model.validateJob(jobName)).isOk();
            softly.assertThat(model.validateJob(NO_REFERENCE_JOB)).isOk();
            softly.assertThat(model.validateJob("")).isOk();
        });
    }

    @Test
    void doCheckReferenceJobShouldBeNOkWithInvalidValue() {
        String referenceJob = "referenceJob";
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getJob(referenceJob)).thenReturn(Optional.empty());
        ModelValidation model = new ModelValidation(jenkins);

        assertThat(model.validateJob(referenceJob))
                .isError()
                .hasMessage("There is no such job - maybe the job has been renamed?");
    }

    @Test
    void doCheckHealthyShouldBeOkWithValidValues() {
        ModelValidation model = new ModelValidation();

        assertThat(model.validateHealthy(1, 2)).isOk();
        assertThat(model.validateUnhealthy(1, 2)).isOk();
        assertThat(model.validateHealthy(2, 3)).isOk();
        assertThat(model.validateUnhealthy(2, 3)).isOk();
        assertThat(model.validateHealthy(200, 300)).isOk();
        assertThat(model.validateUnhealthy(200, 300)).isOk();

        // Special case: both unset or both zero.
        assertThat(model.validateHealthy(0, 0)).isOk();
        assertThat(model.validateUnhealthy(0, 0)).isOk();
    }

    @Test @Issue("JENKINS-55293")
    void doCheckHealthyShouldBeNotOkWithInvalidValues() {
        ModelValidation model = new ModelValidation();

        assertThat(model.validateHealthy(-1, 0))
                .isError().hasMessage(Messages.FieldValidator_Error_NegativeThreshold());
        assertThat(model.validateUnhealthy(-1, 0)).isOk();

        assertThat(model.validateHealthy(0, 1))
                .isError().hasMessage(Messages.FieldValidator_Error_NegativeThreshold());
        assertThat(model.validateUnhealthy(0, 1)).isOk();

        assertThat(model.validateHealthy(1, 0)).isOk();
        assertThat(model.validateUnhealthy(1, 0))
                .isError().hasMessage(Messages.FieldValidator_Error_ThresholdUnhealthyMissing());

        assertThat(model.validateHealthy(1, 1))
                .isError().hasMessage(Messages.FieldValidator_Error_ThresholdOrder());
        assertThat(model.validateUnhealthy(1, 1))
                .isError().hasMessage(Messages.FieldValidator_Error_ThresholdOrder());

        assertThat(model.validateHealthy(1, -1)).isOk();
        assertThat(model.validateUnhealthy(1, -1))
                .isError().hasMessage(Messages.FieldValidator_Error_NegativeThreshold());
    }

    @Test
    void shouldContainEmptyJobPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        ModelValidation model = new ModelValidation(jenkins);
        ComboBoxModel actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(1);
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void shouldContainSingleElementAndPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job job = mock(Job.class);
        String name = "Job Name";
        when(jenkins.getFullNameOf(job)).thenReturn(name);
        when(jenkins.getAllJobs()).thenReturn(Collections.singleton(name));

        ModelValidation model = new ModelValidation(jenkins);

        ComboBoxModel actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(2);
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB, name);
    }   
}
