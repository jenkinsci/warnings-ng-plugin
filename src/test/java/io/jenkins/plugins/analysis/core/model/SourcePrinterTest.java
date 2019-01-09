package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Disabled;
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
    private static final String ICON_URL = "TODO";

    @Test
    void shouldCreateSourceWithoutLineNumber() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.build();

        Document document = Jsoup.parse(printer.render(asStream("format-java.txt"), issue, StringUtils.EMPTY, ICON_URL));
        String expectedFile = toString("format-java.txt");

        assertThat(document.text()).isEqualToIgnoringWhitespace(expectedFile);

        Elements pre = document.getElementsByTag("pre");
        assertThat(pre.text()).isEqualToIgnoringWhitespace(expectedFile);
    }

    @Test
    void shouldNotRenderContentOfJellyFile() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.build();

        assertThat(Jsoup.parse(printer.render(asStream("format-jelly.txt"), issue, StringUtils.EMPTY, ICON_URL)).text())
                .isEmpty();
    }

    @Test @Disabled("Implement Validation")
    void shouldCreateSourceWithLineNumber() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.setLineStart(7).setMessage("Hello Message").build();

        assertThat(printer.render(asStream("format-java.txt"), issue, "description", ICON_URL))
                .isEqualTo("Something");
    }

    @Test @Disabled("Implement Validation")
    void shouldCreateSourceWithoutDescription() {
        IssueBuilder builder = new IssueBuilder();
        SourcePrinter printer = new SourcePrinter();
        Issue issue = builder.setLineStart(7).setMessage("Hello Message").build();

        assertThat(printer.render(asStream("format-java.txt"), issue, StringUtils.EMPTY, ICON_URL))
                .isEqualTo("Something");
    }
}