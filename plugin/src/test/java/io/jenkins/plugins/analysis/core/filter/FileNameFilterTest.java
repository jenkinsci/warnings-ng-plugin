package io.jenkins.plugins.analysis.core.filter;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link FileNameFilter}.
 *
 * @author Michael Trimarchi
 */
class FileNameFilterTest {
    @Test
    void shouldMatchRelativeEntryAgainstAbsolutePath() {
        var filter = new FileNameFilter(List.of("src/File.ts"));

        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/File.ts"))).isTrue();
    }

    @Test
    void shouldMatchExactFileName() {
        var filter = new FileNameFilter(List.of("File.ts"));

        assertThat(filter.test(issue("File.ts"))).isTrue();
    }

    @Test
    void shouldNotMatchPartialSegment() {
        var filter = new FileNameFilter(List.of("Test.java"));

        assertThat(filter.test(issue("src/MyTest.java"))).isFalse();
    }

    @Test
    void shouldNormalizeSeparatorsAndRelativePrefix() {
        var filter = new FileNameFilter(List.of(".\\src\\File.ts"));

        assertThat(filter.test(issue("/home/jenkins/workspace/job/src/File.ts"))).isTrue();
    }

    @Test
    void shouldMatchWindowsStyleIssuePath() {
        var filter = new FileNameFilter(List.of("src/File.ts"));

        assertThat(filter.test(issue("C:\\work\\src\\File.ts"))).isTrue();
    }

    @Test
    void shouldIgnoreBlankEntries() {
        var filter = new FileNameFilter(List.of("", "   ", "src/File.ts"));

        assertThat(filter.test(issue("src/File.ts"))).isTrue();
        assertThat(filter.test(issue("src/Other.ts"))).isFalse();
    }

    @Test
    void shouldRejectEverythingForEmptyList() {
        var filter = new FileNameFilter(List.of());

        assertThat(filter.test(issue("src/File.ts"))).isFalse();
    }

    @Test
    void nullObjectShouldAcceptEverything() {
        var filter = new NullFileNameFilter();

        assertThat(filter.test(issue("src/File.ts"))).isTrue();
    }

    private Issue issue(final String fileName) {
        return new IssueBuilder().setFileName(fileName).build();
    }
}
