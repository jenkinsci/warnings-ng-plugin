package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.parser.dry.CodeDuplication;
import edu.hm.hafner.analysis.parser.dry.CodeDuplication.DuplicationGroup;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryLabelProvider;
import net.sf.json.JSONArray;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the class {@link DuplicateCodeScanner}.
 *
 * @author Ullrich Hafner
 */
class DuplicateCodeScannerTest {
    private static final int EXPECTED_NUMBER_OF_COLUMNS = 5;

    @Test
    void shouldConvertIssueToArrayOfColumns() {
        IssueBuilder builder = new IssueBuilder();
        builder.setReference("1");
        Issue issue = builder.setFileName("/path/to/file-1").setLineStart(15).setLineEnd(29).build();
        Issue duplicate = builder.setFileName("/path/to/file-2").setLineStart(5).setLineEnd(19).build();

        DuplicationGroup group = new DuplicationGroup();
        CodeDuplication first = new CodeDuplication(issue, group);
        CodeDuplication second = new CodeDuplication(duplicate, group);

        DryLabelProvider labelProvider = new DryLabelProvider("id", "name");

        JSONArray firstColumns = labelProvider.toJson(first, build -> String.valueOf(build));
        assertThatJson(firstColumns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS);

        assertThat(firstColumns.getString(0)).matches(createFileLinkMatcher("file-1", 15));
        assertThat(firstColumns.get(1)).isEqualTo("<a href=\"NORMAL\">Normal</a>");
        assertThat(firstColumns.get(2)).isEqualTo(15);
        assertThat(firstColumns.getString(3)).matches(createLinkMatcher("file-2", 5));
        assertThat(firstColumns.get(4)).isEqualTo("1");

        JSONArray secondColumns = labelProvider.toJson(second, build -> String.valueOf(build));
        assertThatJson(secondColumns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS);

        assertThat(secondColumns.getString(0)).matches(createFileLinkMatcher("file-2", 5));
        assertThat(secondColumns.get(1)).isEqualTo("<a href=\"NORMAL\">Normal</a>");
        assertThat(secondColumns.get(2)).isEqualTo(15);
        assertThat(secondColumns.getString(3)).matches(createLinkMatcher("file-1", 15));
        assertThat(secondColumns.get(4)).isEqualTo("1");
    }

    private static String createFileLinkMatcher(final String fileName, final int lineNumber) {
        return "<a href=\\\"source.[0-9a-f-]+/#" + lineNumber + "\\\">"
                + fileName + ":" + lineNumber
                + "</a>";
    }

    private static String createLinkMatcher(final String fileName, final int lineNumber) {
        return String.format("<ul><li>%s</li></ul>",createFileLinkMatcher(fileName, lineNumber));
    }
}