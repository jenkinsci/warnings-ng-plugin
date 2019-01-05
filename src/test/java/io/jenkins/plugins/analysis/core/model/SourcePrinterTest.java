package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.util.ResourceTest;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link SourceDetail}.
 *
 * @author Ullrich Hafner
 */
class SourcePrinterTest extends ResourceTest {
    @Test
    void shouldCreateSourceWithoutLineNumber() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.build();

        assertThat(printer.render(asStream("format-java.txt"), issue, "description"))
                .isEqualTo("Something");
    }

    @Test
    void shouldCreateJellyFile() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.build();

        assertThat(printer.render(asStream("format-jelly.txt"), issue, "description"))
                .isEqualTo("Something");
    }

    @Test
    void shouldCreateSourceWithLineNumber() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.setLineStart(7).setMessage("Hello Message").build();

        assertThat(printer.render(asStream("format-java.txt"), issue, "description"))
                .isEqualTo("Something");
    }

    @Test
    void shouldCreateSourceWithoutDescription() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.setLineStart(7).setMessage("Hello Message").build();

        assertThat(printer.render(asStream("format-java.txt"), issue, StringUtils.EMPTY))
                .isEqualTo("Something");
    }
}