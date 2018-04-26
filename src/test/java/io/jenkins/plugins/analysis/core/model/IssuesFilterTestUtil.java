package io.jenkins.plugins.analysis.core.model;

import java.util.Arrays;
import java.util.function.Predicate;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;
import edu.hm.hafner.analysis.assertj.IssuesAssert;
import static org.assertj.core.api.Assertions.*;

/**
 * An abstract class containing various helping methods for testing classes which extend IssuesFilter.
 * Contains (adjusted) methods from {@link edu.hm.hafner.analysis.IssueFilterTest}
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
abstract class IssuesFilterTestUtil {

    static final String EMPTY_PATTERN = "";

    static final Issue ISSUE1 = new IssueBuilder()
            .setFileName("FileName1")
            .setPackageName("PackageName1")
            .setModuleName("ModuleName1")
            .setCategory("CategoryName1")
            .setType("Type1")
            .build();

    static final Issue ISSUE2 = new IssueBuilder()
            .setFileName("FileName2")
            .setPackageName("PackageName2")
            .setModuleName("ModuleName2")
            .setCategory("CategoryName2")
            .setType("Type2")
            .build();

    static final Issue ISSUE3 = new IssueBuilder()
            .setFileName("FileName3")
            .setPackageName("PackageName3")
            .setModuleName("ModuleName3")
            .setCategory("CategoryName3")
            .setType("Type3")
            .build();

    /**
     * Applies the given filter and checks if result is equal to the expected values.
     *
     * @param criterion
     *         the filter criterion.
     * @param issues
     *         the issues to filter.
     * @param expectedOutput
     *         the expected filter result.
     */
    void applyFilterAndCheckResult(final Predicate<? super Issue> criterion, final Issues<Issue> issues,
            final Issue... expectedOutput) {
        String id = "id";

        issues.setId(id);
        Issues<Issue> result = issues.filter(criterion);

        assertThat(result.iterator()).containsExactly(expectedOutput);

        IssuesAssert.assertThat(result).hasId(id);
    }

    /**
     * Get issues containing the prepared issues 1, 2 and 3.
     *
     * @return issues.
     */
    Issues<Issue> getIssues() {
        Issues<Issue> issues = new Issues<>();
        issues.add(ISSUE1, ISSUE2, ISSUE3);
        return issues;
    }

    /**
     * Returns a predicate for an issue filtered with the given pattern.
     *
     * @param issueFilterBuilder
     *         the issue filter builder object.
     * @param issuesFilter
     *         the issues filter object.
     * @param pattern
     *         the pattern to use.
     *
     * @return the prepared predicate.
     */
    Predicate<? super Issue> buildIssueFilterBuilderWithGivenPatternStructure(
            final IssueFilterBuilder issueFilterBuilder, final IssuesFilter issuesFilter, final String... pattern) {
        if (pattern.length > 0) {
            issuesFilter.apply(issueFilterBuilder, pattern[0]);
            return buildIssueFilterBuilderWithGivenPatternStructure(issueFilterBuilder, issuesFilter,
                    Arrays.copyOfRange(pattern, 1, pattern.length));
        }
        return issueFilterBuilder.build();
    }

}
