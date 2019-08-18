package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeAll;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.FileNameRenderer.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;

import static j2html.TagCreator.*;
import static org.mockito.Mockito.*;

/**
 * Base class for tests of the details models.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.AbstractClassWithoutAnyMethod")
public abstract class AbstractDetailsModelTest {
    static final String DESCRIPTION
            = join("Hello description with", a().withHref("url").withText("link")).render();
    private static final String MESSAGE
            = join("Hello message with", a().withHref("url").withText("link")).render();
    static final String EXPECTED_DESCRIPTION = String.format(
            "<div class=\"details-control\" data-description=\"&lt;p&gt;&lt;strong&gt;%s&lt;/strong&gt;&lt;/p&gt; %s\"></div>",
            StringEscapeUtils.escapeHtml4(MESSAGE), StringEscapeUtils.escapeHtml4(DESCRIPTION));

    private IssueBuilder createBuilder() {
        return new IssueBuilder().setMessage(MESSAGE);
    }

    Issue createIssue(final int index) {
        IssueBuilder builder = createBuilder();
        builder.setFileName("/path/to/file-" + index)
                .setPackageName("package-" + index)
                .setCategory("category-" + index)
                .setType("type-" + index)
                .setLineStart(15)
                .setSeverity(Severity.WARNING_HIGH)
                .setReference("1");
        return builder.build();
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
        return String.format("<a href=\"source.%s/#15\">file-1:15</a>", issue.getId().toString());
    }

    /**
     * Creates a {@link FileNameRenderer} that can access all files.
     *
     * @return a {@link FileNameRenderer} stub
     */
    protected FileNameRenderer createFileNameRenderer() {
        BuildFolderFacade buildFolder = mock(BuildFolderFacade.class);
        when(buildFolder.canAccessAffectedFileOf(any())).thenReturn(true);
        return new FileNameRenderer(buildFolder);
    }

    /**
     * Creates a {@link DefaultAgeBuilder} that shows the age of 1.
     *
     * @return a {@link DefaultAgeBuilder} stub
     */
    protected DefaultAgeBuilder createAgeBuilder() {
        return new DefaultAgeBuilder(1, "url");
    }
}
