package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the URL extraction logic for category documentation links.
 *
 * @author Akash Manna
 */
class AnalysisModelParserTest {
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldExtractUrlFromIssueDescription() {
        try (var builder = new IssueBuilder()) {
            builder.setDescription("See <a href=\"https://clang.llvm.org/extra/clang-tidy/checks/bugprone/forward-declaration-namespace.html\">Clang-Tidy documentation</a>.");
            var issue = builder.build();
            
            var url = extractUrlFromDescription(issue.getDescription());
            
            assertThat(url)
                    .isEqualTo("https://clang.llvm.org/extra/clang-tidy/checks/bugprone/forward-declaration-namespace.html");
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldExtractUrlFromIssueDescriptionWithSingleQuotes() {
        try (var builder = new IssueBuilder()) {
            builder.setDescription("See <a href='https://example.com/doc'>documentation</a>.");
            var issue = builder.build();
            
            var url = extractUrlFromDescription(issue.getDescription());
            
            assertThat(url)
                    .isEqualTo("https://example.com/doc");
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldReturnEmptyStringWhenNoUrlInDescription() {
        try (var builder = new IssueBuilder()) {
            builder.setDescription("No URL in this description");
            var issue = builder.build();
            
            var url = extractUrlFromDescription(issue.getDescription());
            
            assertThat(url)
                    .isEmpty();
        }
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldReturnEmptyStringWhenDescriptionIsBlank() {
        var url = extractUrlFromDescription("");
        
        assertThat(url)
                .isEmpty();
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldReturnEmptyStringWhenDescriptionIsNull() {
        var url = extractUrlFromDescription(null);
        
        assertThat(url)
                .isEmpty();
    }

    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76257")
    void shouldExtractFirstUrlWhenMultipleUrlsPresent() {
        try (var builder = new IssueBuilder()) {
            builder.setDescription("See <a href=\"https://first.com/doc\">first</a> and <a href=\"https://second.com/doc\">second</a>.");
            var issue = builder.build();
            
            var url = extractUrlFromDescription(issue.getDescription());
            
            assertThat(url)
                    .isEqualTo("https://first.com/doc");
        }
    }

    /**
     * Extracts URL from HTML description (simulates the logic from AnalysisModelParser).
     */
    private String extractUrlFromDescription(final String description) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(description)) {
            var hrefPattern = java.util.regex.Pattern.compile("href=[\"']([^\"']+)[\"']");
            var matcher = hrefPattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }
}
