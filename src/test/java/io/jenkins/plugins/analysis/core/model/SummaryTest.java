package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider.IconPathResolver;
import io.jenkins.plugins.analysis.core.model.Summary.LabelProviderFactoryFacade;
import io.jenkins.plugins.analysis.core.quality.AnalysisBuild;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;
import io.jenkins.plugins.analysis.core.quality.Thresholds;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.BallColor;
import hudson.model.Result;

/**
 * Tests the class {@link Summary}.
 *
 * @author Ullrich Hafner
 */
class SummaryTest {
    @Test
    void shouldProvideSummary() {
        Locale.setDefault(Locale.ENGLISH);

        LabelProviderFactoryFacade facade = mock(LabelProviderFactoryFacade.class);
        when(facade.get("checkstyle")).thenReturn(createLabelProvider("checkstyle", "CheckStyle"));
        when(facade.get("pmd")).thenReturn(createLabelProvider("pmd", "PMD"));

        StaticAnalysisRun analysisRun = mock(StaticAnalysisRun.class);
        when(analysisRun.getSizePerOrigin()).thenReturn(Maps.fixedSize.of("checkstyle", 15, "pmd", 20));
        when(analysisRun.getNewSize()).thenReturn(2);
        when(analysisRun.getFixedSize()).thenReturn(2);
        when(analysisRun.getNoIssuesSinceBuild()).thenReturn(1);

        Thresholds thresholds = new Thresholds();
        thresholds.unstableTotalAll = 1;
        when(analysisRun.getQualityGate()).thenReturn(new QualityGate(thresholds));
        when(analysisRun.getOverallResult()).thenReturn(Result.SUCCESS);
        when(analysisRun.getReferenceBuild()).thenReturn(15);

        AnalysisBuild build = mock(AnalysisBuild.class);
        when(build.getNumber()).thenReturn(2);
        when(analysisRun.getBuild()).thenReturn(build);

        String actualSummary = new Summary(createLabelProvider("test", "SummaryTest"), analysisRun, facade).create();
        assertThat(actualSummary).contains("CheckStyle, PMD");
        assertThat(actualSummary).contains("No warnings for 2 builds");
        assertThat(actualSummary).contains("since build <a href=\"../../1/testResult\" class=\"model-link inside\">1</a>");
        assertThat(actualSummary).containsPattern(
                createWarningsLink("<a href=\"testResult/new\">.*2 new warnings.*</a>"));
        assertThat(actualSummary).containsPattern(
                createWarningsLink("<a href=\"testResult/fixed\">.*2 fixed warnings.*</a>"));
        assertThat(actualSummary).contains("Quality gates: <a href=\"BLUE\" alt=\"Success\" title=\"Success\">Success</a>");
        assertThat(actualSummary).contains("Reference build <a href=\"../../15/testResult\" class=\"model-link inside\">15</a>");
    }

    private DefaultLabelProvider createLabelProvider(final String checkstyle, final String checkStyle) {
        return new DefaultLabelProvider(checkstyle, checkStyle, new IconResolverStub());
    }

    private Pattern createWarningsLink(final String href) {
        return Pattern.compile(href, Pattern.MULTILINE | Pattern.DOTALL);
    }

    private static class IconResolverStub extends IconPathResolver {
        @Override
        String getImagePath(final BallColor color) {
            return color.name();
        }
    }
}