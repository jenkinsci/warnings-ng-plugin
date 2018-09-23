package io.jenkins.plugins.analysis.warnings;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.warnings.DuplicateCodeScanner.DryTableModel;

/**
 * Tests the class {@link DryTableModel}.
 *
 * @author Ullrich Hafner
 */
// FIXME: enable using the new model
class DryTableModelTest {
    private static final int EXPECTED_NUMBER_OF_COLUMNS = 6;
    private static final String EMPTY_DETAILS = "<div class=\"details-control\" data-description=\"&lt;p&gt;&lt;strong&gt;&lt;/strong&gt;&lt;/p&gt; description\"></div>";

    @Test
    void shouldConvertIssueToArrayOfColumns() {
//        IssueBuilder builder = new IssueBuilder();
//        builder.setReference("1");
//        DuplicationGroup group = new DuplicationGroup();
//        Issue issue = builder.setFileName("/path/to/file-1")
//                .setLineStart(15)
//                .setLineEnd(29)
//                .setAdditionalProperties(group)
//                .build();
//        Issue duplicate = builder.setFileName("/path/to/file-2")
//                .setLineStart(5)
//                .setLineEnd(19)
//                .setAdditionalProperties(group)
//                .build();
//
//        group.add(issue);
//        group.add(duplicate);
//
//        DryTableModel model = new DryTableModel();
//        Report report = mock(Report.class);
//
//        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
//        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
//        FileNameRenderer fileNameRenderer = new FileNameRenderer(buildFolder);
//
//        List<String> firstColumns = model.getRow(report, issue, String::valueOf, fileNameRenderer, "description");
//        assertThatJson(firstColumns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS);
//
//        assertThat(firstColumns.get(0)).isEqualTo(EMPTY_DETAILS);
//        assertThat(firstColumns.get(1)).matches(createFileLinkMatcher("file-1", 15));
//        assertThat(firstColumns.get(2)).isEqualTo("<a href=\"NORMAL\">Normal</a>");
//        assertThat(firstColumns.get(3)).isEqualTo("15");
//        assertThat(firstColumns.get(4)).matches(createLinkMatcher("file-2", 5));
//        assertThat(firstColumns.get(5)).isEqualTo("1");
//
//        List<String> secondColumns = model.getRow(report, duplicate, String::valueOf, fileNameRenderer, "description");
//        assertThatJson(secondColumns).isArray().ofLength(EXPECTED_NUMBER_OF_COLUMNS);
//
//        assertThat(firstColumns.get(0)).isEqualTo(EMPTY_DETAILS);
//        assertThat(secondColumns.get(1)).matches(createFileLinkMatcher("file-2", 5));
//        assertThat(secondColumns.get(2)).isEqualTo("<a href=\"NORMAL\">Normal</a>");
//        assertThat(secondColumns.get(3)).isEqualTo("15");
//        assertThat(secondColumns.get(4)).matches(createLinkMatcher("file-1", 15));
//        assertThat(secondColumns.get(5)).isEqualTo("1");
    }

    private static String createFileLinkMatcher(final String fileName, final int lineNumber) {
        return "<a href=\\\"source.[0-9a-f-]+/#" + lineNumber + "\\\">"
                + fileName + ":" + lineNumber
                + "</a>";
    }

    private static String createLinkMatcher(final String fileName, final int lineNumber) {
        return String.format("<ul><li>%s</li></ul>", createFileLinkMatcher(fileName, lineNumber));
    }
}