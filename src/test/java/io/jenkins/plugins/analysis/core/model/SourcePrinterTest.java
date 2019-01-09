package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.util.ResourceTest;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link SourcePrinter}.
 *
 * @author Ullrich Hafner
 */
class SourcePrinterTest extends ResourceTest {
    private static final String ICON_URL = "/path/to/icon.png";
    private static final String MESSAGE = "Hello Message";
    private static final String DESCRIPTION = "Hello Description";
    private static final String NO_DESCRIPTION = StringUtils.EMPTY;

    @Test
    void shouldCreateSourceWithoutLineNumber() {
        SourcePrinter printer = new SourcePrinter();

        IssueBuilder builder = new IssueBuilder();
        Issue issue = builder.build();

        Document document = Jsoup.parse(printer.render(asStream("format-java.txt"), issue,
                NO_DESCRIPTION, ICON_URL));
        String expectedFile = toString("format-java.txt");

        assertThat(document.text()).isEqualToIgnoringWhitespace(expectedFile);

        Elements pre = document.getElementsByTag("pre");
        assertThat(pre.text()).isEqualToIgnoringWhitespace(expectedFile);
    }

    @Test
    void shouldNotRenderContentOfJellyFile() {
        IssueBuilder builder = new IssueBuilder();
        Issue issue = builder.build();

        SourcePrinter printer = new SourcePrinter();

        Document document = Jsoup.parse(printer.render(asStream("format-jelly.txt"), issue,
                NO_DESCRIPTION, ICON_URL));
        assertThat(document.text()).isEmpty();
    }

    @Test
    void shouldCreateSourceWithLineNumber() {
        IssueBuilder builder = new IssueBuilder();
        Issue issue = builder.setLineStart(7).setMessage(MESSAGE).build();

        SourcePrinter printer = new SourcePrinter();

        Document document = Jsoup.parse(printer.render(asStream("format-java.txt"), issue,
                DESCRIPTION, ICON_URL));

        assertThatCodeIsEqualToSourceText(document);

        assertThat(document.getElementsByClass("analysis-warning-title").text())
                .isEqualTo(MESSAGE);
        assertThat(document.getElementsByClass("analysis-detail").text())
                .isEqualTo(DESCRIPTION);
        assertThat(document.getElementsByClass("collapse-panel").text())
                .isEqualTo(DESCRIPTION);
    }

    private void assertThatCodeIsEqualToSourceText(final Document document) {
        Elements code = document.getElementsByTag("code");
        assertThat(code.text()).isEqualToIgnoringWhitespace(toString("format-java.txt"));
    }

    @Test
    void shouldCreateSourceWithoutDescription() {
        IssueBuilder builder = new IssueBuilder();
        Issue issue = builder.setLineStart(7).setMessage("Hello Message").build();

        SourcePrinter printer = new SourcePrinter();

        Document document = Jsoup.parse(printer.render(asStream("format-java.txt"), issue,
                NO_DESCRIPTION, ICON_URL));

        assertThatCodeIsEqualToSourceText(document);

        assertThat(document.getElementsByClass("analysis-warning-title").text())
                .isEqualTo(MESSAGE);
        assertThat(document.getElementsByClass("analysis-detail")).isEmpty();
        assertThat(document.getElementsByClass("collapse-panel")).isEmpty();
    }
}