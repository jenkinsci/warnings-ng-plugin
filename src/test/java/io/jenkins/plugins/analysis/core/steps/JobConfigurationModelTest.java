package io.jenkins.plugins.analysis.core.steps;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.JenkinsFacade;
import static io.jenkins.plugins.analysis.core.steps.IssuesRecorder.*;
import io.jenkins.plugins.analysis.core.testutil.Assertions;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static io.jenkins.plugins.analysis.core.testutil.SoftAssertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Job;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;

/**
 * Tests the class {@link JobConfigurationModel}.
 *
 * @author Arne Schöntag
 * @author Stephan Plöderl
 * @author Ullrich Hafner
 */
class JobConfigurationModelTest {
    @Test
    void shouldValidateCharsets() {
        JobConfigurationModel model = new JobConfigurationModel();

        assertSoftly(softly -> {
            softly.assertThat(model.validateCharset("")).isOk();
            softly.assertThat(model.validateCharset("UTF-8")).isOk();
            softly.assertThat(model.validateCharset("Some wrong text"))
                    .isError()
                    .hasMessage(JobConfigurationModel.createWrongEncodingErrorMessage());
        });
    }

    @Test
    void shouldContainDefaultCharsets() {
        JobConfigurationModel model = new JobConfigurationModel();

        ComboBoxModel allCharsets = model.getAllCharsets();
        assertThat(allCharsets).isNotEmpty().contains("UTF-8", "ISO-8859-1");
    }

    @Test
    void shouldFallbackToPlatformCharset() {
        JobConfigurationModel model = new JobConfigurationModel();
        
        assertThat(model.getCharset("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(model.getCharset("nothing")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset("")).isEqualTo(Charset.defaultCharset());
        assertThat(model.getCharset(null)).isEqualTo(Charset.defaultCharset());
    }

    @Test
    void doFillMinimumPriorityItemsShouldBeNotEmpty() {
        JobConfigurationModel model = new JobConfigurationModel();
        ListBoxModel allFilters = model.getAllSeverityFilters();

        assertThat(allFilters.size()).isEqualTo(3);

        Option actualHighOption = allFilters.get(0);
        Option actualNormalOption = allFilters.get(1);
        Option actualLowOption = allFilters.get(2);

        assertSoftly(softly -> {
            softly.assertThat(actualHighOption.value).isEqualTo(Priority.HIGH.name());
            softly.assertThat(actualHighOption.name).isEqualTo(Messages.PriorityFilter_High());
            softly.assertThat(actualNormalOption.value).isEqualTo(Priority.NORMAL.name());
            softly.assertThat(actualNormalOption.name).isEqualTo(Messages.PriorityFilter_Normal());
            softly.assertThat(actualLowOption.value).isEqualTo(Priority.LOW.name());
            softly.assertThat(actualLowOption.name).isEqualTo(Messages.PriorityFilter_Low());
        });
    }

    @Test
    void doFillReferenceJobItemsShouldBeNotEmpty() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getAllJobs()).thenReturn(new HashSet<>());

        JobConfigurationModel model = new JobConfigurationModel(jenkins);

        assertThat(model.getAllJobs()).containsExactly(NO_REFERENCE_JOB);
    }

    @Test
    void doCheckReferenceJobShouldBeOkWithValidValues() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        Job<?, ?> job = mock(Job.class);
        String jobName = "referenceJob";
        when(jenkins.getJob(jobName)).thenReturn(Optional.of(job));
        JobConfigurationModel model = new JobConfigurationModel(jenkins);

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
        JobConfigurationModel model = new JobConfigurationModel(jenkins);

        assertThat(model.validateJob(referenceJob))
                .isError()
                .hasMessage("There is no such job - maybe the job has been renamed?");
    }

    @Test
    void doCheckHealthyShouldBeOkWithValidValues() {
        // healthy = 0 = unhealthy
        JobConfigurationModel model = new JobConfigurationModel();
        FormValidation actualResult = model.validateHealthy(0, 0);
        Assertions.assertThat(actualResult).isOk();

        // healthy < unhealthy
        actualResult = model.validateHealthy(1, 2);
        Assertions.assertThat(actualResult).isOk();
    }

    @Test
    void doCheckHealthyShouldBeNotOkWithInvalidValues() {
        // healthy < 0
        JobConfigurationModel model = new JobConfigurationModel();
        FormValidation actualResult = model.validateHealthy(-1, 0);
        Assertions.assertThat(actualResult).isError();

        // healthy = 0 , unhealthy > 0
        actualResult = model.validateHealthy(0, 1);
        Assertions.assertThat(actualResult).isError();

        // healthy > 0 , unhealthy > healthy
        actualResult = model.validateHealthy(2, 1);
        Assertions.assertThat(actualResult).isError();
    }

    @Test
    void doCheckUnhealthyShouldBeOkWithValidValues() {
        // unhealthy > healthy > 0
        JobConfigurationModel model = new JobConfigurationModel();
        FormValidation actualResult = model.validateUnhealthy(1, 2);
        Assertions.assertThat(actualResult).isOk();

        // unhealthy > healthy = 0
        actualResult = model.validateUnhealthy(0, 1);
        Assertions.assertThat(actualResult).isOk();
    }

    @Test
    void doCheckUnhealthyShouldBeNotOkWithInvalidValues() {
        // healthy > unhealthy = 0
        JobConfigurationModel model = new JobConfigurationModel();
        FormValidation actualResult = model.validateUnhealthy(1, 0);
        Assertions.assertThat(actualResult).isError();

        // healthy > unhealthy > 0
        actualResult = model.validateUnhealthy(1, 1);
        Assertions.assertThat(actualResult).isError();

        // unhealthy < 0
        actualResult = model.validateUnhealthy(0, -1);
        Assertions.assertThat(actualResult).isError();
    }

    @Test
    void shouldContainEmptyJobPlaceHolder() {
        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        JobConfigurationModel model = new JobConfigurationModel(jenkins);
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

        JobConfigurationModel model = new JobConfigurationModel(jenkins);

        ComboBoxModel actualModel = model.getAllJobs();

        assertThat(actualModel).hasSize(2);
        assertThat(actualModel).containsExactly(NO_REFERENCE_JOB, name);
    }   
}
