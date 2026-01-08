package io.jenkins.plugins.analysis.core.model;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeAll;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

import java.util.Locale;
import java.util.stream.Stream;

import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.datatables.DetailedCell;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.util.JenkinsFacade;

import static j2html.TagCreator.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Base class for tests of the details models.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractDetailsModelTest {
    private static final String REL_CONTENT = "nofollow noopener noreferrer";
    static final String DESCRIPTION
            = join("Hello description with", a().withHref("url").withText("link").withRel(REL_CONTENT)).render();
    private static final String MESSAGE
            = join("Hello message with", a().withHref("url").withText("link").withRel(REL_CONTENT)).render();
    /** Details icon that opens a new row. */
    protected static final String DETAILS_ICON = "my-symbol my-symbol";
    static final String EXPECTED_DESCRIPTION = String.format(
            "<div class=\"details-control\" data-description=\"&lt;p&gt;&lt;strong&gt;%s&lt;/strong&gt;&lt;/p&gt; %s\">"
                    + DETAILS_ICON + "</div>",
            StringEscapeUtils.escapeHtml4(StringEscapeUtils.escapeHtml4(MESSAGE)),
            StringEscapeUtils.escapeHtml4(DESCRIPTION));

    @MustBeClosed
    private IssueBuilder createBuilder() {
        return new IssueBuilder().setMessage(MESSAGE);
    }

    Issue createIssue(final int index) {
        try (var builder = createBuilder()) {
            builder.setFileName("/path/to/file-" + index)
                    .setPackageName("package-" + index)
                    .setCategory("category-" + index)
                    .setType("type-" + index)
                    .setLineStart(15)
                    .setSeverity(Severity.WARNING_HIGH)
                    .setReference("1");
            return builder.build();
        }
    }

    @BeforeAll
    static void useEnglishLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    /**
     * Creates a link to the affected file that is used in the file name column.
     *
     * @param issue
     *         the issue
     *
     * @return the file name column
     */
    protected String createExpectedFileName(final Issue issue) {
        return "<a href=\"source.%s/#15\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"/path/to/file-1\">file-1:15</a>".formatted(issue.getId().toString());
    }

    /**
     * Creates a {@link FileNameRenderer} that can access all files.
     *
     * @return a {@link FileNameRenderer} stub
     */
    protected FileNameRenderer createFileNameRenderer() {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any(), any())).thenReturn(true);
        return new FileNameRenderer(mock(Run.class), buildFolder);
    }

    /**
     * Creates a {@link DefaultAgeBuilder} that shows the age of 1.
     *
     * @return a {@link DefaultAgeBuilder} stub
     */
    protected DefaultAgeBuilder createAgeBuilder() {
        return new DefaultAgeBuilder(1, "url", createProject());
    }

    private Job<?, ?> createProject() {
        var job = mock(Job.class);
        var run = mock(Run.class);
        when(job.getBuild(anyString())).thenReturn(run);
        return job;
    }

    protected void assertThatDetailedColumnContains(final DetailedCell<String> actualColumn,
            final String expectedDisplayName, final String expectedSortOrder) {
        assertThat(actualColumn.getDisplay()).isEqualTo(expectedDisplayName);
        assertThat(actualColumn.getSort()).isEqualTo(expectedSortOrder);
    }

    protected Stream<String> getLabels(final DetailsTableModel model) {
        return model.getColumns().stream().map(TableColumn::getHeaderLabel);
    }

    protected JenkinsFacade createJenkinsFacade() {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getSymbol(any())).thenReturn("my-symbol");
        return jenkinsFacade;
    }

    protected void verifyColumnProperty(final DetailsTableModel model, final int column, final String property) {
        assertThatJson(model.getColumnsDefinition())
                .node("[" + column + "]")
                .node("data")
                .isEqualTo(property);
    }

    protected void verifyFileNameColumn(final String columnDefinitions) {
        assertThatJson(columnDefinitions).node("[1].render._").isEqualTo("display");
        assertThatJson(columnDefinitions).node("[1].render.sort").isEqualTo("sort");
    }
}
