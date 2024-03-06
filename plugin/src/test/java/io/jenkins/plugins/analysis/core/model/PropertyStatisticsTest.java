package io.jenkins.plugins.analysis.core.model;

import java.util.NoSuchElementException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import com.google.errorprone.annotations.MustBeClosed;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * PropertyStatisticsTest to test {@link PropertyStatistics}.
 *
 * @author Martin Weibel
 */
class PropertyStatisticsTest {
    private static final String KEY = "key";

    @Test
    void shouldHandleEmptyReport() {
        PropertyStatistics statistics = createStatistics(new Report());

        assertThat(statistics)
                .hasTotal(0)
                .hasTotalNewIssues(0)
                .hasNoKeys()
                .hasProperty("category")
                .hasMax(0);

        assertThatExceptionIsThrownBy(() -> statistics.getCount("error"));
        assertThatExceptionIsThrownBy(() -> statistics.getErrorCount("error"));
        assertThatExceptionIsThrownBy(() -> statistics.getHighCount("error"));
        assertThatExceptionIsThrownBy(() -> statistics.getNormalCount("error"));
        assertThatExceptionIsThrownBy(() -> statistics.getLowCount("error"));

        assertThat(statistics.getNewCount("error")).isEqualTo(0);
        assertThat(statistics.getDisplayName("error")).isEqualTo("error");
        assertThat(statistics.getToolTip("error")).isEqualTo(StringUtils.EMPTY);
    }

    private void assertThatExceptionIsThrownBy(
            final ThrowingCallable callable) {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(callable)
                .withMessageContaining("error");
    }

    @Test
    void shouldHandleReportWithOneIssue() {
        try (IssueBuilder builder = createBuilder()) {
            Report issues = new Report();
            issues.add(builder.setCategory("error").build());

            PropertyStatistics statistics = createStatistics(issues);

            assertThat(statistics).hasTotal(1)
                    .hasTotalNewIssues(0)
                    .hasOnlyKeys("error")
                    .hasProperty("category")
                    .hasMax(1);

            assertThat(statistics.getCount("error")).isEqualTo(1);
            assertThat(statistics.getErrorCount("error")).isEqualTo(0);
            assertThat(statistics.getHighCount("error")).isEqualTo(1);
            assertThat(statistics.getNormalCount("error")).isEqualTo(0);
            assertThat(statistics.getLowCount("error")).isEqualTo(0);

            assertThat(statistics.getNewCount("error")).isEqualTo(0);
            assertThat(statistics.getDisplayName("error")).isEqualTo("error");
            assertThat(statistics.getToolTip("error")).isEqualTo(StringUtils.EMPTY);
        }
    }

    private PropertyStatistics createStatistics(final Report issues) {
        return new PropertyStatistics(issues, new Report(),
                "category", Function.identity());
    }

    /**
     * Verifies that getTotal() returns the total number of issues if there is one issue.
     */
    @Test
    void shouldHandleReportWithTwoIssues() {
        try (IssueBuilder builder = createBuilder()) {
            Report issues = new Report();
            issues.add(builder.setCategory("errorA").build());
            issues.add(builder.setCategory("errorB").build());

            PropertyStatistics statistics = createStatistics(issues);

            assertThat(statistics).hasTotal(2)
                    .hasTotalNewIssues(0)
                    .hasOnlyKeys("errorA", "errorB")
                    .hasProperty("category")
                    .hasMax(1);

            assertThat(statistics.getCount("errorA")).isEqualTo(1);
            assertThat(statistics.getErrorCount("errorA")).isEqualTo(0);
            assertThat(statistics.getHighCount("errorA")).isEqualTo(1);
            assertThat(statistics.getNormalCount("errorA")).isEqualTo(0);
            assertThat(statistics.getLowCount("errorA")).isEqualTo(0);

            assertThat(statistics.getNewCount("errorA")).isEqualTo(0);
            assertThat(statistics.getDisplayName("errorA")).isEqualTo("errorA");
            assertThat(statistics.getToolTip("errorA")).isEqualTo(StringUtils.EMPTY);

            assertThat(statistics.getCount("errorB")).isEqualTo(1);
            assertThat(statistics.getErrorCount("errorB")).isEqualTo(0);
            assertThat(statistics.getHighCount("errorB")).isEqualTo(1);
            assertThat(statistics.getNormalCount("errorB")).isEqualTo(0);
            assertThat(statistics.getLowCount("errorB")).isEqualTo(0);

            assertThat(statistics.getNewCount("errorB")).isEqualTo(0);
            assertThat(statistics.getDisplayName("errorB")).isEqualTo("errorB");
            assertThat(statistics.getToolTip("errorB")).isEqualTo(StringUtils.EMPTY);
        }
    }

    @Test
    void shouldReturnToolTip() {
        PropertyStatistics statistics = new PropertyStatistics(
                new Report(), new Report(), "category",
                string -> KEY.equals(string) ? KEY : "tooltip");

        assertThat(statistics.getDisplayName(KEY)).isEqualTo(KEY);
        assertThat(statistics.getToolTip(KEY)).isEmpty();

        assertThat(statistics.getDisplayName("name")).isEqualTo("tooltip");
        assertThat(statistics.getToolTip("name")).isEqualTo("name");
    }

    @Test
    void shouldReplaceEmptyStringInDisplayName() {
        try (IssueBuilder builder = createBuilder()) {
            Report issues = new Report();
            issues.add(builder.setCategory("").build());

            PropertyStatistics statistics = createStatistics(issues);

            assertThat(statistics).hasTotal(1)
                    .hasTotalNewIssues(0)
                    .hasOnlyKeys("")
                    .hasProperty("category")
                    .hasMax(1);

            assertThat(statistics.getCount("")).isEqualTo(1);
            assertThat(statistics.getErrorCount("")).isEqualTo(0);
            assertThat(statistics.getHighCount("")).isEqualTo(1);
            assertThat(statistics.getNormalCount("")).isEqualTo(0);
            assertThat(statistics.getLowCount("")).isEqualTo(0);

            assertThat(statistics.getNewCount("")).isEqualTo(0);
            assertThat(statistics.getDisplayName("")).isEqualTo("-");
            assertThat(statistics.getToolTip("")).isEqualTo(StringUtils.EMPTY);
        }
    }

    @Test
    void shouldReturnMaxWhenTwoIssuesHaveSameCategory() {
        try (IssueBuilder builder = createBuilder()) {
            Report issues = new Report();
            issues.add(builder.setCategory("ab").setPackageName("P1").build());
            issues.add(builder.setCategory("ab").setPackageName("P2").build());
            PropertyStatistics statistics = createStatistics(issues);

            verifyTwoIssuesWithSameCategory(statistics, 2);
        }
    }

    /**
     * Verifies that getMax() returns the maximum number of issues for each property instance.
     */
    @Test
    void shouldReturnMaxValueDifferentCategories() {
        try (IssueBuilder builder = createBuilder()) {
            Report issues = new Report();
            issues.add(builder.setCategory("ab").setPackageName("P1").build());
            issues.add(builder.setCategory("ab").setPackageName("P2").build());
            issues.add(builder.setCategory("abc").setPackageName("P2").build());
            PropertyStatistics statistics = createStatistics(issues);

            verifyTwoIssuesWithSameCategory(statistics, 3);
            assertThat(statistics).hasKeys("ab", "abc");
        }
    }

    @MustBeClosed @SuppressWarnings("resource")
    private IssueBuilder createBuilder() {
        return new IssueBuilder().setSeverity(Severity.WARNING_HIGH);
    }

    private void verifyTwoIssuesWithSameCategory(final PropertyStatistics statistics, final int total) {
        assertThat(statistics)
                .hasTotal(total)
                .hasTotalNewIssues(0)
                .hasKeys("ab")
                .hasProperty("category")
                .hasMax(2);

        assertThat(statistics.getCount("ab")).isEqualTo(2);
        assertThat(statistics.getErrorCount("ab")).isEqualTo(0);
        assertThat(statistics.getHighCount("ab")).isEqualTo(2);
        assertThat(statistics.getNormalCount("ab")).isEqualTo(0);
        assertThat(statistics.getLowCount("ab")).isEqualTo(0);

        assertThat(statistics.getNewCount("ab")).isEqualTo(0);
        assertThat(statistics.getDisplayName("ab")).isEqualTo("ab");
        assertThat(statistics.getToolTip("ab")).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void shouldReturnTwoNewIssues() {
        try (IssueBuilder builder = new IssueBuilder()) {
            Report issues = new Report();
            issues.add(builder.setCategory(KEY).setOrigin("A").build());
            issues.add(builder.setCategory(KEY).setOrigin("B").build());
            PropertyStatistics statistics = new PropertyStatistics(issues, issues, "category", Function.identity());

            assertThat(statistics)
                    .hasTotal(2)
                    .hasTotalNewIssues(2)
                    .hasOnlyKeys(KEY)
                    .hasProperty("category")
                    .hasMax(2);

            assertThat(statistics.getNewCount(KEY)).isEqualTo(2);
            assertThat(statistics.getNewCount("other")).isEqualTo(0);
        }
    }
}
