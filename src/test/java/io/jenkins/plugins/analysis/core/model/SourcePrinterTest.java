package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.util.ResourceTest;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link SourcePrinter}.
 *
 * @author Ullrich Hafner
 */
class SourcePrinterTest extends ResourceTest {
    @Test
    void shouldCreateSourceWithoutLineNumber() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.build();

        Document document = Jsoup.parse(printer.render(asStream("format-java.txt"), issue, StringUtils.EMPTY));
        assertThat(document.text()).isEqualToIgnoringWhitespace(toString("format-java.txt"));

        assertThat(document.getElementsByTag("pre"))
                .isEqualTo("Something");
    }

    @Test
    void shouldNotRenderContentOfJellyFile() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.build();

        assertThat(Jsoup.parse(printer.render(asStream("format-jelly.txt"), issue, StringUtils.EMPTY)).text())
                .isEmpty();
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