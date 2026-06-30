package io.jenkins.plugins.analysis.core.filter;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link SuppressionFilter}.
 *
 * @see <a href="https://github.com/jenkinsci/warnings-ng-plugin/issues/3051">JENKINS-65553</a>
 */
@org.junitpioneer.jupiter.Issue("JENKINS-65553")
class SuppressionFilterTest {
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();

    // ------------------------------------------------------------------ isActive

    @Test
    void shouldBeActiveWhenOnlyFilePatternIsSet() {
        var filter = new SuppressionFilter("src/legacy/.*");

        assertThat(filter.isActive()).isTrue();
    }

    @Test
    void shouldBeActiveWhenOnlyMessagePatternIsSet() {
        var filter = new SuppressionFilter("");
        filter.setMessagePattern(".*deprecated.*");

        assertThat(filter.isActive()).isTrue();
    }

    @Test
    void shouldBeActiveWhenBothPatternsAreSet() {
        var filter = new SuppressionFilter("src/legacy/.*");
        filter.setMessagePattern(".*deprecated.*");

        assertThat(filter.isActive()).isTrue();
    }

    @Test
    void shouldBeInactiveWhenBothPatternsAreBlank() {
        var filter = new SuppressionFilter("");
        filter.setMessagePattern("");

        assertThat(filter.isActive()).isFalse();
    }

    // ------------------------------------------------------------------ compound suppression (file AND message)

    @Test
    void shouldSuppressIssueWhenBothFilenameAndMessageMatch() {
        var filter = new SuppressionFilter(".*legacy.*");
        filter.setMessagePattern(".*deprecated.*");

        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("src/legacy/Foo.java").setMessage("use of deprecated API").build());

        var predicate = filter.getFilterPredicate();
        assertThat(predicate).isNotNull();

        var filtered = report.filter(predicate);
        assertThat(filtered).isEmpty();
    }

    @Test
    void shouldKeepIssueWhenOnlyFilenameMatchesButMessageDoesNot() {
        var filter = new SuppressionFilter(".*legacy.*");
        filter.setMessagePattern(".*deprecated.*");

        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("src/legacy/Foo.java").setMessage("null pointer dereference").build());

        var predicate = filter.getFilterPredicate();
        assertThat(predicate).isNotNull();

        var filtered = report.filter(predicate);
        assertThat(filtered).hasSize(1);
    }

    @Test
    void shouldKeepIssueWhenOnlyMessageMatchesButFilenameDoesNot() {
        var filter = new SuppressionFilter(".*legacy.*");
        filter.setMessagePattern(".*deprecated.*");

        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("src/main/Bar.java").setMessage("use of deprecated API").build());

        var predicate = filter.getFilterPredicate();
        assertThat(predicate).isNotNull();

        var filtered = report.filter(predicate);
        assertThat(filtered).hasSize(1);
    }

    @Test
    void shouldKeepIssueWhenNeitherPatternMatches() {
        var filter = new SuppressionFilter(".*legacy.*");
        filter.setMessagePattern(".*deprecated.*");

        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("src/main/Bar.java").setMessage("null pointer dereference").build());

        var predicate = filter.getFilterPredicate();
        assertThat(predicate).isNotNull();

        var filtered = report.filter(predicate);
        assertThat(filtered).hasSize(1);
    }

    @Test
    void shouldActLikeExcludeFileWhenOnlyFilePatternIsSet() {
        // blank messagePattern matches all messages, so suppresses all issues in legacy files
        var filter = new SuppressionFilter(".*legacy.*");

        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("src/legacy/Foo.java").setMessage("any warning").build());
        report.add(ISSUE_BUILDER.setFileName("src/main/Bar.java").setMessage("any warning").build());

        var predicate = filter.getFilterPredicate();
        assertThat(predicate).isNotNull();

        var filtered = report.filter(predicate);
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0)).hasFileName("src/main/Bar.java");
    }

    @Test
    void shouldActLikeExcludeMessageWhenOnlyMessagePatternIsSet() {
        // blank filePattern matches all files, so suppresses all issues with matching message
        var filter = new SuppressionFilter("");
        filter.setMessagePattern(".*deprecated.*");

        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("src/main/Foo.java").setMessage("use of deprecated API").build());
        report.add(ISSUE_BUILDER.setFileName("src/main/Bar.java").setMessage("null pointer dereference").build());

        var predicate = filter.getFilterPredicate();
        assertThat(predicate).isNotNull();

        var filtered = report.filter(predicate);
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0)).hasMessage("null pointer dereference");
    }

    // ------------------------------------------------------------------ multiple independent rules

    @Test
    void multipleSuppressionRulesWorkAsAnIndependentList() {
        // Rule 1: suppress deprecated warnings in legacy code
        var rule1 = new SuppressionFilter(".*legacy.*");
        rule1.setMessagePattern(".*deprecated.*");

        // Rule 2: suppress all warnings in generated code (no message pattern = match all)
        var rule2 = new SuppressionFilter(".*generated.*");

        var report = new Report();
        // matches rule 1 - should be suppressed
        report.add(ISSUE_BUILDER.setFileName("src/legacy/Foo.java").setMessage("use of deprecated API").build());
        // matches rule 2 - should be suppressed
        report.add(ISSUE_BUILDER.setFileName("target/generated/Bar.java").setMessage("null pointer").build());
        // matches neither - should be kept
        report.add(ISSUE_BUILDER.setFileName("src/main/Baz.java").setMessage("null pointer").build());
        // rule1 file matches but message does not - should be kept
        report.add(ISSUE_BUILDER.setFileName("src/legacy/Old.java").setMessage("null pointer").build());

        var predicate1 = rule1.getFilterPredicate();
        var predicate2 = rule2.getFilterPredicate();

        assertThat(predicate1).isNotNull();
        assertThat(predicate2).isNotNull();

        // Each predicate returns true = keep; AND them to require both rules to say keep
        var combined = predicate1.and(predicate2);
        var filtered = report.filter(combined);

        assertThat(filtered).hasSize(2);
        var fileNames = filtered.stream().map(issue -> issue.getFileName()).toList();
        assertThat(fileNames).containsExactlyInAnyOrder("src/main/Baz.java", "src/legacy/Old.java");
    }

    // ------------------------------------------------------------------ getFilterPredicate always returns non-null

    @Test
    void getFilterPredicateShouldNeverReturnNull() {
        assertThat(new SuppressionFilter(".*").getFilterPredicate()).isNotNull();
        assertThat(new SuppressionFilter("").getFilterPredicate()).isNotNull();

        var withMessage = new SuppressionFilter(".*");
        withMessage.setMessagePattern(".*deprecated.*");
        assertThat(withMessage.getFilterPredicate()).isNotNull();
    }
}
