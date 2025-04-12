package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.analysis.Severity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import hudson.model.Job;

import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
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
    void doFillMinimumPriorityItemsShouldBeNotEmpty() {
        var model = new ModelValidation();
        var allFilters = model.getAllSeverityFilters();

        assertThat(allFilters.size()).isEqualTo(Severity.getPredefinedValues().size());

        var actualErrorOption = allFilters.get(0);
        assertThat(actualErrorOption.value).isEqualTo(Severity.ERROR.getName());
        assertThat(actualErrorOption.name).isEqualTo(Messages.SeverityFilter_Error());

        var actualHighOption = allFilters.get(1);
        assertThat(actualHighOption.value).isEqualTo(Severity.WARNING_HIGH.getName());
        assertThat(actualHighOption.name).isEqualTo(Messages.SeverityFilter_High());

        var actualNormalOption = allFilters.get(2);
        assertThat(actualNormalOption.value).isEqualTo(Severity.WARNING_NORMAL.getName());
        assertThat(actualNormalOption.name).isEqualTo(Messages.SeverityFilter_Normal());

        var actualLowOption = allFilters.get(3);
        assertThat(actualLowOption.value).isEqualTo(Severity.WARNING_LOW.getName());
        assertThat(actualLowOption.name).isEqualTo(Messages.SeverityFilter_Low());
    }

    @Test
    void doFillTrendChartOptions() {
        var model = new ModelValidation();
        var allFilters = model.getAllTrendChartTypes();

        assertThat(allFilters).hasSize(TrendChartType.values().length);
    }

    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getAllJobNames()).thenReturn(new HashSet<>());

        var model = new ModelValidation(jenkins);

        assertThat(model.getAllJobs()).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        var jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));
        var model = new ModelValidation(jenkins);

        assertThat(model.validateJob(jobName)).isOk();
        assertThat(model.validateJob(NO_REFERENCE_JOB)).isOk();
        assertThat(model.validateJob("")).isOk();
    }

    @Test
    void doCheckReferenceJobShouldBeNOkWithInvalidValue() {
        var referenceJob = "referenceJob";
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getJob(referenceJob)).thenReturn(Optional.empty());
        var model = new ModelValidation(jenkins);

        assertThat(model.validateJob(referenceJob))
                .isError()
                .hasMessage("There is no such job - maybe the job has been renamed?");
    }

    @Test
    void doCheckHealthyShouldBeOkWithValidValues() {
        var model = new ModelValidation();

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

    @Test
    @Issue("JENKINS-55293")
    void doCheckHealthyShouldBeNotOkWithInvalidValues() {
        var model = new ModelValidation();

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
        var model = new ModelValidation(jenkins);
        var actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(1);
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void shouldContainSingleElementAndPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        var name = "Job Name";
        when(jenkins.getFullNameOf(job)).thenReturn(name);
        when(jenkins.getAllJobNames()).thenReturn(Set.of(name));

        var model = new ModelValidation(jenkins);

        var actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(2);
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB, name);
    }
}
