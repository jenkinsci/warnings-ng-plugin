package io.jenkins.plugins.analysis.warnings;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.warnings.RegisteredParser.Descriptor;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link RegisteredParser}.
 *
 * @author Ullrich Hafner
 */
class RegisteredParserTest {
    private static final String CHECKSTYLE_ID = "checkstyle";
    private static final String CHECK_STYLE_NAME = "CheckStyle";
    private static final String CHECKSTYLE_PATTERN = "**/checkstyle-result.xml";

    @Test
    void shouldThrowExceptionIfThereIsNoParserAvailable() {
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> new RegisteredParser("-unknown-"));
    }

    @Test
    void shouldAllowChangingId() {
        RegisteredParser parser = new RegisteredParser(CHECKSTYLE_ID);

        JenkinsFacade jenkins = mock(JenkinsFacade.class);
        when(jenkins.getDescriptorOrDie(RegisteredParser.class)).thenReturn(new RegisteredParser.Descriptor());
        parser.setJenkinsFacade(jenkins);

        assertThat(parser.createParser()).isInstanceOf(CheckStyleParser.class);
        assertThat(parser)
                .hasAnalysisModelId(CHECKSTYLE_ID)
                .hasId(CHECKSTYLE_ID)
                .hasActualId(CHECKSTYLE_ID)
                .hasActualName(CHECK_STYLE_NAME)
                .hasActualPattern(CHECKSTYLE_PATTERN);

        assertThat(parser.getLabelProvider()).hasId(CHECKSTYLE_ID);
        assertThat(parser.getLabelProvider()).hasName(CHECK_STYLE_NAME);

        String customId = "customId";
        parser.setId(customId);
        String customName = "Custom Name";
        parser.setName(customName);
        String customPattern = "Custom Pattern";
        parser.setPattern(customPattern);

        assertThat(parser)
                .hasAnalysisModelId(CHECKSTYLE_ID)
                .hasId(customId)
                .hasActualId(customId)
                .hasActualName(customName)
                .hasActualPattern(customPattern);

        assertThat(parser.getLabelProvider()).hasId(CHECKSTYLE_ID); // get decorations for checkstyle
        assertThat(parser.getLabelProvider()).hasName(customName);
    }

    @Nested
    class DescriptorTest {
        @Test
        void shouldPopulateListOfParsers() {
            JenkinsFacade jenkins = mock(JenkinsFacade.class);
            when(jenkins.hasPermission(Jenkins.READ)).thenReturn(true);

            Descriptor descriptor = new Descriptor(jenkins);
            assertThat(descriptor.getId()).isEqualTo(Descriptor.ANALYSIS_MODEL_ID);
            assertThat(descriptor.doFillAnalysisModelIdItems()).extracting(o -> o.value).first().isEqualTo("acu-cobol");

            when(jenkins.hasPermission(Jenkins.READ)).thenReturn(false);
            assertThat(descriptor.doFillAnalysisModelIdItems()).isEmpty();
        }
    }
}
