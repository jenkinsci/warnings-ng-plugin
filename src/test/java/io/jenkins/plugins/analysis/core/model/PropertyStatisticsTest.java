package io.jenkins.plugins.analysis.core.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;

import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.util.NoSuchElementException;
import static io.jenkins.plugins.analysis.core.model.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PropertyStatisticsTest to test {@link PropertyStatistics}.
 *
 * @author Martin Weibel
 */
class PropertyStatisticsTest {

    /**
     * Verifies that getTotal() returns the total number of issues if there is one issues.
     */
    @Test
    void shouldReturnTotalNumber() {
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasTotal(0);
    }

    /**
     * Verifies that getProperty() returns the name of this property.
     */
    @Test
    void shouldReturnPropertyString() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        String actualProperty = statistics.getProperty();

        assertThat(actualProperty).isEqualTo("category");
    }

    /**
     * Verifies that getProperty() doesnt returns anything if there is no property.
     */
    @Test
    void shouldReturnEmptyPropertyString() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "", Function.identity());

        String actualProperty = statistics.getProperty();

        assertThat(actualProperty).isEmpty();
    }

    /**
     * Verifies that getDisplayName() returns a display name for the specified property instance when key is valid.
     */
    @Test
    void shouldReturnDisplayNameString() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        String actualDisplayName = statistics.getDisplayName("name");

        assertThat(actualDisplayName).isEqualTo("name");
    }

    /**
     * Verifies that getDisplayName() returns nothing if key is an empty string.
     */
    @Test
    void shouldReturnEmptyDisplayNameString() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        String actualDisplayName = statistics.getDisplayName("");

        assertThat(actualDisplayName).isEmpty();
    }

    /**
     * Verifies that getKeys() returns one instances for this property.
     */
    @Test
    void shouldReturnKeys() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("key").build());

        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThat(statistics).hasOnlyKeys("key");
    }

    /**
     * Verifies that getKeys() returns all instances for this property.
     */
    @Test
    void shouldReturnAllKeys() {
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        Set<String> actualProperty = statistics.getKeys();

        assertThat(actualProperty).isEmpty();
    }

    /**
     * Verifies that getMax() returns zero if there are no issues.
     */
    @Test
    void shouldReturnMaxValueZero() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        int value = statistics.getMax();

        assertThat(value).isEqualTo(0);
    }

    /**
     * Verifies that getMax() returns one if there is one issues.
     */
    @Test
    void shouldReturnMaxValue() {
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
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
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThrows(NoSuchElementException.class, ()->statistics.getCount("key"), "There are no issues available for the key 'key'.");
    }

    /**
     * Verifies that getCount() returns one if there is one issues for the specified property instance.
     */
    @Test
    void shouldReturnCountOne() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getCount("key");

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getCount() returns the maximum number of issues for the specified property instance.
     */
    @Test
    void shouldReturnCountThree() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setCategory("key").build());
        issues.add(builder.setCategory("key").setPackageName("P1").build());
        issues.add(builder.setCategory("key").setPackageName("P2").build());
        issues.add(builder.setCategory("key1").setPackageName("P1").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getCount("key");

        assertThat(value).isEqualTo(3);
    }

    /**
     * Verifies that getHighCount() returns the number of issues with Priority#HIGH.
     */
    @Test
    void shouldReturnHighCountOne() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.HIGH).setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getHighCount("key");

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getHighCount() returns the number of issues with Priority#HIGH.
     */
    @Test
    void shouldReturnHighCountTwo() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.HIGH).setCategory("key").setOrigin("A").build());
        issues.add(builder.setPriority(Priority.HIGH).setCategory("key").setOrigin("B").build());
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").setOrigin("B").build());
        issues.add(builder.setPriority(Priority.NORMAL).setCategory("key").setOrigin("B").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getHighCount("key");

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getHighCount() returns zero if there are not issues with Priority#HIGH.
     */
    @Test
    void shouldReturnHighCountZero() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getHighCount("key");

        assertThat(value).isEqualTo(0);
    }

    /**
     * Verifies that getHighCount() throw null pointer exception.
     */
    @Test
    void shouldReturnHighCountException() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThrows(NoSuchElementException.class, ()->statistics.getHighCount("key"), "There are no issues available for the key 'key'.");
    }

    /**
     * Verifies that getNormalCount() returns the number of issues with Priority#NORMAL.
     */
    @Test
    void shouldReturnNormalCountOne() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.NORMAL).setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getNormalCount("key");

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getNormalCount() returns the number of issues with Priority#NORMAL.
     */
    @Test
    void shouldReturnNormalCountTwo() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.NORMAL).setCategory("key").setOrigin("A").build());
        issues.add(builder.setPriority(Priority.NORMAL).setCategory("key").setOrigin("B").build());
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").setOrigin("B").build());
        issues.add(builder.setPriority(Priority.HIGH).setCategory("key").setOrigin("B").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getNormalCount("key");

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getNormalCount() throw null pointer exception.
     */
    @Test
    void shouldReturnNormalCountException() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThrows(NoSuchElementException.class, ()->statistics.getNormalCount("key"), "There are no issues available for the key 'key'.");
    }

    /**
     * Verifies that getNormalCount() returns zero if there are not issues with Priority#NORMAL.
     */
    @Test
    void shouldReturnNormalCountZero() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getNormalCount("key");

        assertThat(value).isEqualTo(0);
    }

    /**
     * Verifies that getLowCount() returns the number of issues with Priority#LOW.
     */
    @Test
    void shouldReturnLowCountOne() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getLowCount("key");

        assertThat(value).isEqualTo(1);
    }

    /**
     * Verifies that getLowCount() throw null pointer exception.
     */
    @Test
    void shouldReturnLowCountException() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        assertThrows(NoSuchElementException.class, ()->statistics.getLowCount("key"), "There are no issues available for the key 'key'.");
    }

    /**
     * Verifies that getLowCount() returns the number of issues with Priority#LOW.
     */
    @Test
    void shouldReturnLowCountTwo() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").setOrigin("A").build());
        issues.add(builder.setPriority(Priority.LOW).setCategory("key").setOrigin("B").build());
        issues.add(builder.setPriority(Priority.NORMAL).setCategory("key").setOrigin("B").build());
        issues.add(builder.setPriority(Priority.HIGH).setCategory("key").setOrigin("B").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getLowCount("key");

        assertThat(value).isEqualTo(2);
    }

    /**
     * Verifies that getLowCount() returns zero if there are not issues with Priority#LOW.
     */
    @Test
    void shouldReturnLowCountZero() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        issues.add(builder.setPriority(Priority.HIGH).setCategory("key").build());
        PropertyStatistics statistics = new PropertyStatistics(issues, "category", Function.identity());

        long value = statistics.getLowCount("key");

        assertThat(value).isEqualTo(0);
    }
}