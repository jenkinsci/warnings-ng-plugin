package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.analysis.IssueBuilder;

import java.util.Locale;

import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.CompositeLocalizable;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link StaticAnalysisLabelProvider}.
 *
 * @author Ullrich Hafner
 */
class StaticAnalysisLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String OTHER_NAME = "other";

    @Test
    void shouldShowDescriptionOfIssueByDefault() {
        try (var issueBuilder = new IssueBuilder()) {
            var labelProvider = new StaticAnalysisLabelProvider("description", "-");

            var text = "Hello Description";
            issueBuilder.setDescription(text);
            assertThat(labelProvider.getDescription(issueBuilder.build())).isEqualTo(text);

            var emptyDescription = new StaticAnalysisLabelProvider("description", "-", i -> "empty");
            assertThat(emptyDescription.getDescription(issueBuilder.build())).isEqualTo("empty");
        }
    }

    @Test
    void shouldIgnoreUndefinedName() {
        var labelProvider = new StaticAnalysisLabelProvider("cpd", "-");

        assertThat(labelProvider).hasId("cpd");
        assertThat(labelProvider).hasName(labelProvider.getDefaultName());
        assertThat(labelProvider).hasLinkName("Static Analysis Warnings");
    }

    @Test @Issue("JENKINS-61834, JENKINS-67245")
    void shouldNotEscapeHtmlEntitiesAnymore() {
        var labelProvider = new StaticAnalysisLabelProvider(ID, "C++");

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName("C++");
        assertThat(labelProvider.getLinkName()).contains("C++");
        assertThat(labelProvider.getTrendName()).contains("C++");
    }

    @Test
    void shouldReturnIdAndNameOfConstructorParametersInAllDisplayProperties() {
        var labelProvider = new StaticAnalysisLabelProvider(ID, NAME);

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName(NAME);
        assertThat(labelProvider.getLinkName()).contains(NAME);
        assertThat(labelProvider.getTrendName()).contains(NAME);

        labelProvider.setName(OTHER_NAME);
        assertThat(labelProvider).hasName(OTHER_NAME);
        assertThat(labelProvider.getLinkName()).contains(OTHER_NAME);
        assertThat(labelProvider.getTrendName()).contains(OTHER_NAME);
    }

    @Test
    void shouldReturnIdAndDefaultNameIfNoNameIsGiven() {
        var emptyNameLabelProvider = new StaticAnalysisLabelProvider(ID, "");

        assertThat(emptyNameLabelProvider).hasId(ID);
        assertThat(emptyNameLabelProvider).hasName(emptyNameLabelProvider.getDefaultName());

        var nullNameLabelProvider = new StaticAnalysisLabelProvider(ID, null);

        assertThat(nullNameLabelProvider).hasId(ID);
        assertThat(nullNameLabelProvider).hasName(nullNameLabelProvider.getDefaultName());

        var noNameLabelProvider = new StaticAnalysisLabelProvider(ID);

        assertThat(noNameLabelProvider).hasId(ID);
        assertThat(noNameLabelProvider).hasName(noNameLabelProvider.getDefaultName());
    }

    /**
     * Tests the class {@link AgeBuilder}.
     */
    @Nested
    class AgeBuilderTest {
        @Test
        void shouldCreateAgeLinkForFirstBuild() {
            var builder = new DefaultAgeBuilder(1, "checkstyle/", createProject());

            assertThat(builder.apply(1)).isEqualTo("1");
        }

        private Job<?, ?> createProject() {
            var job = mock(Job.class);
            var run = mock(Run.class);
            when(job.getBuild(anyString())).thenReturn(run);
            return job;
        }

        @Test
        void shouldCreateAgeLinkForPreviousBuilds() {
            var builder = new DefaultAgeBuilder(10, "checkstyle/", createProject());
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../1/checkstyle\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../9/checkstyle\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }

        @Test @Issue("JENKINS-65845")
        void shouldCreatePlainTextForDeletedBuilds() {
            var builder = new DefaultAgeBuilder(10, "checkstyle/", mock(Job.class));
            assertThat(builder.apply(1)).isEqualTo("10");
            assertThat(builder.apply(9)).isEqualTo("2");
            assertThat(builder.apply(10)).isEqualTo("1");
        }

        @Test
        void shouldCreateAgeLinkForSubDetails() {
            var builder = new DefaultAgeBuilder(10, "checkstyle/package.1234/", createProject());
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../../1/checkstyle\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../../9/checkstyle\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }
    }

    /**
     * Tests the utility class {@link CompositeLocalizable}.
     */
    @Nested
    class CompositeLocalizableTest {
        @Test
        void shouldCreateComposedMessage() {
            var localizable = new CompositeLocalizable("Static Analysis", Messages._Tool_NoIssues());

            assertThat(localizable).hasToString("Static Analysis: No warnings");
            assertThat(localizable.toString(Locale.ENGLISH)).isEqualTo("Static Analysis: No warnings");
            assertThat(localizable.getKey()).isEqualTo(Messages._Tool_NoIssues().getKey());
        }
    }
}
