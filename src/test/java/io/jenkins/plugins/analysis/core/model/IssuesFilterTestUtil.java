package io.jenkins.plugins.analysis.core.model;

import java.util.Arrays;
import java.util.function.Predicate;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;

/**
 * An abstract class containing various helping methods for testing classes which extend IssuesFilter. Contains
 * (adjusted) methods from {@link edu.hm.hafner.analysis.IssueFilterTest}
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
class IssuesFilterTestUtil {
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
    void applyFilterAndCheckResult(final Predicate<? super Issue> criterion, final Report issues,
            final Issue... expectedOutput) {
        String origin = "origin";
        String reference = "reference";

        issues.setOrigin(origin);
        issues.setReference(reference);

        Report result = issues.filter(criterion);

        assertThat(result.iterator()).containsExactly(expectedOutput);

        assertThat(result).hasOrigin(origin);
        assertThat(result).hasReference(reference);
    }

    /**
     * Get issues containing the prepared issues 1, 2 and 3.
     *
     * @return issues.
     */
    Report getIssues() {
        return new Report().addAll(ISSUE1, ISSUE2, ISSUE3);
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
