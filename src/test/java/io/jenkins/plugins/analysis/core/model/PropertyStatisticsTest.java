package io.jenkins.plugins.analysis.core.model;

import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.NoSuchElementException;
import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * PropertyStatisticsTest to test {@link PropertyStatistics}.
 *
 * @author Martin Weibel
 */
class PropertyStatisticsTest {
    private static final String KEY = "key";

    /**
     * Verifies that getTotal() returns the total number of issues if there is one issues.
     */
    @Test
    void shouldReturnTotalNumber() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("error").build());

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasTotal(issues.size());
    }

    /**
     * Verifies that getTotal() returns the total number of issues if there is one issues.
     */
    @Test
    void shouldReturnTotalNumberMoreIssues() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("errorA").build());
        issues.add(builder.setCategory("errorB").build());

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasTotal(2);
    }

    /**
     * Verifies that getTotal() returns the total number of issues if there are no issues.
     */
    @Test
    void shouldReturnTotalNumberZero() {
        Report issues = new Report();

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasTotal(0);
    }

    /**
     * Verifies that getProperty() returns the name of this property.
     */
    @Test
    void shouldReturnPropertyString() {
        PropertyStatistics statistics = new PropertyStatistics(new Report(), "category", Function.identity());

        String actualProperty = statistics.getProperty();

        assertThat(actualProperty).isEqualTo("category");
    }

    /**
     * Verifies that getDisplayName() returns a display name for the specified property instance when key is valid.
     */
    @Test
    void shouldReturnDisplayNameString() {
        PropertyStatistics statistics = new PropertyStatistics(new Report(), "category", Function.identity());

        String actualDisplayName = statistics.getDisplayName("name");

        assertThat(actualDisplayName).isEqualTo("name");
    }

    /**
     * Verifies that getKeys() returns one instances for this property.
     */
    @Test
    void shouldReturnKeys() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory(KEY).build());

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasOnlyKeys(KEY);
    }

    /**
     * Verifies that getKeys() returns all instances for this property.
     */
    @Test
    void shouldReturnAllKeys() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("keyA").build());
        issues.add(builder.setCategory("keyB").build());

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasOnlyKeys("keyA", "keyB");
    }

    /**
     * Verifies that getKeys() returns empty string if the instances for this property is empty.
     */
    @Test
    void shouldReturnEmptyStringKeys() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("").build());

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasOnlyKeys("");
    }

    /**
     * Verifies that getKeys() returns nothing if there are no instances for this property.
     */
    @Test
    void shouldReturnEmptyKeys() {
        PropertyStatistics statistics = new PropertyStatistics(new Report(), "category", Function.identity());

        Set<String> actualProperty = statistics.getKeys();

        assertThat(actualProperty).isEmpty();
    }

    /**
     * Verifies that getMax() returns zero if there are no issues.
     */
    @Test
    void shouldReturnMaxValueZero() {
        Report issues = new Report();

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        int value = statistics.getMax();

        assertThat(value).isEqualTo(0);
    }

    /**
     * Verifies that getMax() returns one if there is one issues.
     */
    @Test
    void shouldReturnMaxValue() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        int value = statistics.getMax();

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getMax() returns the maximum number of issues for each property instance.
     */
    @Test
    void shouldReturnMaxValueTwo() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("ab").setPackageName("P1").build());
        issues.add(builder.setCategory("ab").setPackageName("P2").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        int value = statistics.getMax();

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getMax() returns the maximum number of issues for each property instance.
     */
    @Test
    void shouldReturnMaxValueDifferentCategories() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("ab").setPackageName("P1").build());
        issues.add(builder.setCategory("ab").setPackageName("P2").build());
        issues.add(builder.setCategory("abc").setPackageName("P2").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        int value = statistics.getMax();

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getCount() throw an exception.
     */
    @Test
    void shouldReturnCountEmpty() {
        PropertyStatistics statistics = new PropertyStatistics(new Report(), "category", Function.identity());

        String key = KEY;
        assertThatThrownBy(() -> statistics.getCount(key))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(key);
    }

    /**
     * Verifies that getCount() returns one if there is one issues for the specified property instance.
     */
    @Test
    void shouldReturnCountOne() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getCount(KEY);

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getCount() returns the maximum number of issues for the specified property instance.
     */
    @Test
    void shouldReturnCountThree() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory(KEY).build());
        issues.add(builder.setCategory(KEY).setPackageName("P1").build());
        issues.add(builder.setCategory(KEY).setPackageName("P2").build());
        issues.add(builder.setCategory("key1").setPackageName("P1").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getCount(KEY);

        assertThat(value).isEqualTo(3);
    }

    /**
     * Verifies that getHighCount() returns the number of issues with Severity#HIGH.
     */
    @Test
    void shouldReturnHighCountOne() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_HIGH).setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getHighCount(KEY);

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getHighCount() returns the number of issues with Severity#HIGH.
     */
    @Test
    void shouldReturnHighCountTwo() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_HIGH).setCategory(KEY).setOrigin("A").build());
        issues.add(builder.setSeverity(Severity.WARNING_HIGH).setCategory(KEY).setOrigin("B").build());
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).setOrigin("B").build());
        issues.add(builder.setSeverity(Severity.WARNING_NORMAL).setCategory(KEY).setOrigin("B").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getHighCount(KEY);

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getHighCount() returns zero if there are not issues with Severity#HIGH.
     */
    @Test
    void shouldReturnHighCountZero() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getHighCount(KEY);

        assertThat(value).isEqualTo(0);
    }

    /**
     * Verifies that getHighCount() throw null pointer exception.
     */
    @Test
    void shouldReturnHighCountException() {
        Report issues = new Report();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThatThrownBy(() -> statistics.getHighCount(KEY))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(KEY);
    }

    /**
     * Verifies that getNormalCount() returns the number of issues with Severity#NORMAL.
     */
    @Test
    void shouldReturnNormalCountOne() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_NORMAL).setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getNormalCount(KEY);

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getNormalCount() returns the number of issues with Severity#NORMAL.
     */
    @Test
    void shouldReturnNormalCountTwo() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_NORMAL).setCategory(KEY).setOrigin("A").build());
        issues.add(builder.setSeverity(Severity.WARNING_NORMAL).setCategory(KEY).setOrigin("B").build());
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).setOrigin("B").build());
        issues.add(builder.setSeverity(Severity.WARNING_HIGH).setCategory(KEY).setOrigin("B").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getNormalCount(KEY);

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getNormalCount() throw null pointer exception.
     */
    @Test
    void shouldReturnNormalCountException() {
        PropertyStatistics statistics = new PropertyStatistics(new Report(), "category", Function.identity());

        assertThatThrownBy(() -> statistics.getNormalCount(KEY))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(KEY);
    }

    /**
     * Verifies that getNormalCount() returns zero if there are not issues with Severity#NORMAL.
     */
    @Test
    void shouldReturnNormalCountZero() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getNormalCount(KEY);

        assertThat(value).isEqualTo(0);
    }

    /**
     * Verifies that getLowCount() returns the number of issues with Severity#LOW.
     */
    @Test
    void shouldReturnLowCountOne() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getLowCount(KEY);

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getLowCount() throw null pointer exception.
     */
    @Test
    void shouldReturnLowCountException() {
        PropertyStatistics statistics = new PropertyStatistics(new Report(), "category", Function.identity());

        assertThatThrownBy(() -> statistics.getLowCount(KEY))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(KEY);
    }

    /**
     * Verifies that getLowCount() returns the number of issues with Severity#LOW.
     */
    @Test
    void shouldReturnLowCountTwo() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).setOrigin("A").build());
        issues.add(builder.setSeverity(Severity.WARNING_LOW).setCategory(KEY).setOrigin("B").build());
        issues.add(builder.setSeverity(Severity.WARNING_NORMAL).setCategory(KEY).setOrigin("B").build());
        issues.add(builder.setSeverity(Severity.WARNING_HIGH).setCategory(KEY).setOrigin("B").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getLowCount(KEY);

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getLowCount() returns zero if there are not issues with Severity#LOW.
     */
    @Test
    void shouldReturnLowCountZero() {
        Report issues = new Report();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setSeverity(Severity.WARNING_HIGH).setCategory(KEY).build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getLowCount(KEY);

        assertThat(value).isEqualTo(0);
    }
}