package io.jenkins.plugins.analysis.core.testutil;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.IssuesDetail;

/**
 * Assertions for {@link IssuesDetail}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion", "PMD.LinguisticNaming"})
public class IssuesDetailAssert extends AbstractAssert<IssuesDetailAssert, IssuesDetail> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";

    /**
     * Creates a new {@link IssuesDetailAssert} to make assertions on actual {@link IssuesDetail}.
     *
     * @param actual
     *         the issue we want to make assertions on
     */
    public IssuesDetailAssert(final IssuesDetail actual) {
        super(actual, IssuesDetailAssert.class);
    }

    /**
     * An entry point for {@link IssuesDetailAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(myIssues)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link IssuesDetailAssert}
     */
    public static IssuesDetailAssert assertThat(final IssuesDetail actual) {
        return new IssuesDetailAssert(actual);
    }

    /**
     * Verifies that the actual issues of the {@link IssuesDetail} instance are equal to the expected ones.
     *
     * @param expectedIssues
     *         the expected issues to compare the actual {@link Report} to.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual {@link Report} are not equal to the given ones.
     */
    public IssuesDetailAssert hasIssues(final Report expectedIssues) {
        isNotNull();

        if (!Objects.equals(actual.getIssues(), expectedIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedIssues, actual.getIssues());
        }
        return this;
    }
    
    /**
     * Verifies that the actual new issues of the {@link IssuesDetail} instance are equal to the expected ones.
     *
     * @param expectedNewIssues
     *         the expected new issues to compare the actual {@link Report} to.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual {@link Report} are not equal to the given ones.
     */
    public IssuesDetailAssert hasNewIssues(final Report expectedNewIssues) {
        isNotNull();

        if (!Objects.equals(actual.getNewIssues(), expectedNewIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "newIssues", actual, expectedNewIssues, actual.getNewIssues());
        }
        return this;
    }
    
    /**
     * Verifies that the actual fixed issues of the {@link IssuesDetail} instance are equal to the expected ones.
     *
     * @param expectedFixedIssues
     *         the expected fixed issues to compare the actual {@link Report} to.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual {@link Report} are not equal to the given ones.
     */
    public IssuesDetailAssert hasFixedIssues(final Report expectedFixedIssues) {
        isNotNull();

        if (!Objects.equals(actual.getFixedIssues(), expectedFixedIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "fixedIssues", actual, expectedFixedIssues, actual.getFixedIssues());
        }
        return this;
    }
    
    /**
     * Verifies that the actual outstanding issues of the {@link IssuesDetail} instance are equal to the expected ones.
     *
     * @param expectedOutstandingIssues
     *         the expected outstanding issues to compare the actual {@link Report} to.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual {@link Report} are not equal to the given ones.
     */
    public IssuesDetailAssert hasOutstandingIssues(final Report expectedOutstandingIssues) {
        isNotNull();

        if (!Objects.equals(actual.getOutstandingIssues(), expectedOutstandingIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "fixedIssues", actual, expectedOutstandingIssues, actual.getOutstandingIssues());
        }
        return this;
    }
}
