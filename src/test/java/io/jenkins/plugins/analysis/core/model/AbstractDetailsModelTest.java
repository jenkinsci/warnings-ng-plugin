package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

import static j2html.TagCreator.*;

/**
 * Base class for tests of the details models.
 *
 * @author Ullrich Hafner
 */
public abstract class AbstractDetailsModelTest {
    protected static final String EXPECTED_DESCRIPTION = "<div class=\"details-control\" data-description=\"&lt;p&gt;&lt;strong&gt;Hello message with &lt;a href=&quot;url&quot;&gt;link&lt;/a&gt;&lt;/strong&gt;&lt;/p&gt; d\"></div>";
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
