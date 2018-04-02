package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.model.ExcludeCategory.DescriptorImpl;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link ExcludeCategory}.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
class ExcludeCategoryTest extends IssuesFilterTestUtil {

    private static final String FILTER_NAME_TO_CHECK = "CategoryName1";
    private static final String DESCRIPTOR_NAME = "Exclude categories";

    /**
     * Verifies that the given filter is handled correctly. This test case checks for positive results.
     */
    @Test
    void shouldExcludeIssueWithFilterNameToCheck1PositiveTestCase() {
        final Predicate<? super Issue> filter = buildIssueFilterBuilderWithGivenPatternStructure(
                new IssueFilterBuilder(), getIssuesFilterInstance(),
                FILTER_NAME_TO_CHECK);

        applyFilterAndCheckResult(filter, getIssues(), true, ISSUE2, ISSUE3);
    }

    /**
     * Verifies that the given filter is handled correctly. This test case checks for negative results.
     */
    @Test
    void shouldExcludeIssueWithFilterNameToCheck1NegativeTestCase() {
        final Predicate<? super Issue> filter = buildIssueFilterBuilderWithGivenPatternStructure(
                new IssueFilterBuilder(), getIssuesFilterInstance(),
                FILTER_NAME_TO_CHECK);

        applyFilterAndCheckResult(filter, getIssues(), false, ISSUE1);
    }

    /**
     * Verifies that an empty filter is handled correctly.
     */
    @Test
    void shouldExcludeNoIssueWhenPatternIsEmpty() {
        final Predicate<? super Issue> filter = buildIssueFilterBuilderWithGivenPatternStructure(
                new IssueFilterBuilder(), getIssuesFilterInstance(),
                EMPTY_PATTERN);

        applyFilterAndCheckResult(filter, getIssues(), true, ISSUE1, ISSUE2, ISSUE3);
    }

    /**
     * Verifies that a filter covering all possibilities is handled correctly.
     */
    @Test
    void shouldPassNoneWhenAGeneralFilterIsAdded() {
        final Predicate<? super Issue> filter = buildIssueFilterBuilderWithGivenPatternStructure(
                new IssueFilterBuilder(), getIssuesFilterInstance(),
                "[a-zA-Z1-3]*");

        applyFilterAndCheckResult(filter, getIssues(), true);
    }

    /**
     * Verifies that a filter covering no possibilities is handled correctly.
     */
    @Test
    void shouldPassAllWhenUselessFilterIsAdded() {
        final Predicate<? super Issue> conditionWithMultiplePattern = buildIssueFilterBuilderWithGivenPatternStructure(
                new IssueFilterBuilder(), getIssuesFilterInstance(), "[a-zA-Z99]*", "[a-zA-Z98]*", "[a-zA-Z97]*");

        applyFilterAndCheckResult(conditionWithMultiplePattern, getIssues(), true, ISSUE1, ISSUE2, ISSUE3);
    }

    /**
     * Verifies that a NullPointerException is thrown in case the pattern is null.
     */
    @Test
    void shouldThrowNullPointerExceptionForAPatternWhichIsNull() {
        final Predicate<? super Issue> filter = buildIssueFilterBuilderWithGivenPatternStructure(
                new IssueFilterBuilder(), getIssuesFilterInstance(),
                (String) null);

        assertThatNullPointerException().isThrownBy(() -> applyFilterAndCheckResult(filter, getIssues(), false))
                .withNoCause();
    }

    /**
     * Verifies that a NullPointerException is thrown in case the issues filter is null.
     */
    @Test
    void shouldThrowNullPointerExceptionForAnIssueFilterBuilderWhichIsNull() {
        assertThatNullPointerException().isThrownBy(
                () -> buildIssueFilterBuilderWithGivenPatternStructure(new IssueFilterBuilder(), null,
                        FILTER_NAME_TO_CHECK))
                .withNoCause();
    }

    /**
     * Verifies that a NullPointerException is thrown in case the issues filter and the pattern is null.
     */
    @Test
    void shouldThrowNullPointerExceptionForAnIssueFilterBuilderAndAPatternWhichAreNull() {
        assertThatNullPointerException().isThrownBy(
                () -> buildIssueFilterBuilderWithGivenPatternStructure(new IssueFilterBuilder(), null, (String) null))
                .withNoCause();
    }

    /**
     * Verifies that the class implementing serializable is serializable.
     */
    @Test
    void shouldBeSerializable() throws IOException {
        checkForIsSerializable(getIssuesFilterInstance());
    }

    /**
     * Verifies that the descriptor of the instance to check is correct.
     */
    @Test
    void shouldReturnExpectedDescriptor() {
        ExcludeCategoryDescriptorImplAssert.assertThat(new DescriptorImpl()).hasDisplayName(DESCRIPTOR_NAME);
    }

    private IssuesFilter getIssuesFilterInstance() {
        return new ExcludeCategory();
    }
}
