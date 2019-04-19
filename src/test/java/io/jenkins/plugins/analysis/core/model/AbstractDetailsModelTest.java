package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

import static j2html.TagCreator.*;

/**
 * Base class for tests of the details models.
 */
abstract class AbstractDetailsModelTest {
    static final String DESCRIPTION
            = join("Hello description with", a().withHref("url").withText("link")).render();
    static final String MESSAGE
            = join("Hello message with", a().withHref("url").withText("link")).render();

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
}
