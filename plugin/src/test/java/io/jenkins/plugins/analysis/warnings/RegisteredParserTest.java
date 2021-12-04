package io.jenkins.plugins.analysis.warnings;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the class {@link RegisteredParser}.
 *
 * @author Ullrich Hafner
 */
class RegisteredParserTest {
    private static final String CHECKSTYLE_ID = "checkstyle";

    @Test
    void shouldThrowExceptionIfThereIsNoParserAvailable() {
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> new RegisteredParser("-unknown-"));
    }

    @Test
    void shouldAllowChangingId() {
        RegisteredParser parser = new RegisteredParser(CHECKSTYLE_ID);

        assertThat(parser.createParser()).isInstanceOf(CheckStyleParser.class);
        assertThat(parser).hasId(CHECKSTYLE_ID);
        assertThat(parser.getLabelProvider()).hasId(CHECKSTYLE_ID);
        assertThat(parser.getLabelProvider()).hasName("CheckStyle");

    }
}
