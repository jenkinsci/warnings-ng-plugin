package io.jenkins.plugins.analysis.core.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
            final boolean isPositiveTest, final Issue... expectedOutput) {
        final String id = "id";

        issues.setId(id);
        final Issues<Issue> result = issues.filter(criterion);
        if (isPositiveTest) {
            assertThat(result.iterator()).containsExactly(expectedOutput);
        }
        else {
            assertThat(result.iterator()).doesNotContain(expectedOutput);
        }
        IssuesAssert.assertThat(result).hasId(id);
    }

    /**
     * Get issues containing the prepared issues 1, 2 and 3.
     *
     * @return issues.
     */
    Issues<Issue> getIssues() {
        final Issues<Issue> issues = new Issues<>();
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

    /**
     * Asserts that the given class is serializable.
     *
     * @param issuesFilter
     *         the given issues filter.
     */
    void checkForIsSerializable(final IssuesFilter issuesFilter) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(issuesFilter);
        objectOutputStream.close();
        assertThat(byteArrayOutputStream.toByteArray().length > 0).isTrue();
    }

}
