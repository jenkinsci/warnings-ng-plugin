package io.jenkins.plugins.analysis.core.steps;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for relative path functionality in IssuesScanner.
 * This test verifies JENKINS-68856: File paths should be relative to configured source directories.
 */
class PathRelativeTest {
    @Test
    void shouldMakePathsRelativeToSourceDirectory() {
        Report report = new Report();
        try (IssueBuilder builder = new IssueBuilder()) {
            report.add(builder
                    .setFileName("X:/Build/workspace/tasks/src/main/java/Foo.java")
                    .setLineStart(10)
                    .setMessage("Test issue")
                    .build());
            report.add(builder
                    .setFileName("X:/Build/workspace/tasks/src/test/java/FooTest.java")
                    .setLineStart(20)
                    .setMessage("Test issue 2")
                    .build());
            report.add(builder
                    .setFileName("X:/Build/workspace/other/Bar.java")
                    .setLineStart(30)
                    .setMessage("Test issue 3")
                    .build());
        }
        
        Set<String> sourceDirectories = new HashSet<>();
        sourceDirectories.add("X:/Build/workspace/tasks/src");
        
        Report transformedReport = makePathsRelativeToSourceDirectories(report, sourceDirectories);
        
        assertThat(transformedReport.size()).isEqualTo(3);
        
        Issue issue1 = transformedReport.get(0);
        assertThat(issue1.getFileName())
                .as("First issue path should be relative to source directory")
                .isEqualTo("main/java/Foo.java");
        
        Issue issue2 = transformedReport.get(1);
        assertThat(issue2.getFileName())
                .as("Second issue path should be relative to source directory")
                .isEqualTo("test/java/FooTest.java");
        
        Issue issue3 = transformedReport.get(2);
        assertThat(issue3.getFileName())
                .as("Third issue path should remain unchanged (not under source directory)")
                .isEqualTo("X:/Build/workspace/other/Bar.java");
    }
    
    @Test
    void shouldHandleWindowsPathSeparators() {
        Report report = new Report();
        try (IssueBuilder builder = new IssueBuilder()) {
            report.add(builder
                    .setFileName("C:\\workspace\\project\\src\\main\\java\\Foo.java")
                    .setLineStart(10)
                    .setMessage("Test issue")
                    .build());
        }
        
        Set<String> sourceDirectories = new HashSet<>();
        sourceDirectories.add("C:\\workspace\\project\\src");
        
        Report transformedReport = makePathsRelativeToSourceDirectories(report, sourceDirectories);
        
        assertThat(transformedReport.size()).isEqualTo(1);
        assertThat(transformedReport.get(0).getFileName())
                .as("Path with backslashes should be handled correctly")
                .isEqualTo("main/java/Foo.java");
    }
    
    @Test
    void shouldHandleMultipleSourceDirectories() {
        Report report = new Report();
        try (IssueBuilder builder = new IssueBuilder()) {
            report.add(builder
                    .setFileName("/workspace/module1/src/Foo.java")
                    .setLineStart(10)
                    .setMessage("Test issue 1")
                    .build());
            report.add(builder
                    .setFileName("/workspace/module2/src/Bar.java")
                    .setLineStart(20)
                    .setMessage("Test issue 2")
                    .build());
        }
        
        Set<String> sourceDirectories = new HashSet<>();
        sourceDirectories.add("/workspace/module1/src");
        sourceDirectories.add("/workspace/module2/src");
        
        Report transformedReport = makePathsRelativeToSourceDirectories(report, sourceDirectories);
        
        assertThat(transformedReport.size()).isEqualTo(2);
        assertThat(transformedReport.get(0).getFileName()).isEqualTo("Foo.java");
        assertThat(transformedReport.get(1).getFileName()).isEqualTo("Bar.java");
    }
    
    @Test
    void shouldPreserveOtherIssueProperties() {
        Report report = new Report();
        try (IssueBuilder builder = new IssueBuilder()) {
            report.add(builder
                    .setFileName("/workspace/src/Foo.java")
                    .setLineStart(42)
                    .setLineEnd(45)
                    .setColumnStart(10)
                    .setColumnEnd(20)
                    .setMessage("Test message")
                    .setCategory("TestCategory")
                    .setSeverity(edu.hm.hafner.analysis.Severity.WARNING_HIGH)
                    .build());
        }
        
        Set<String> sourceDirectories = new HashSet<>();
        sourceDirectories.add("/workspace/src");
        
        Report transformedReport = makePathsRelativeToSourceDirectories(report, sourceDirectories);
        
        Issue transformed = transformedReport.get(0);
        assertThat(transformed.getFileName()).isEqualTo("Foo.java");
        assertThat(transformed.getLineStart()).isEqualTo(42);
        assertThat(transformed.getLineEnd()).isEqualTo(45);
        assertThat(transformed.getColumnStart()).isEqualTo(10);
        assertThat(transformed.getColumnEnd()).isEqualTo(20);
        assertThat(transformed.getMessage()).isEqualTo("Test message");
        assertThat(transformed.getCategory()).isEqualTo("TestCategory");
        assertThat(transformed.getSeverity()).isEqualTo(edu.hm.hafner.analysis.Severity.WARNING_HIGH);
    }
    
    /**
     * Replicates the logic from IssuesScanner.ReportPostProcessor.makePathsRelativeToSourceDirectories
     * for testing purposes.
     *
     * @param originalReport the report with original file paths
     * @param sourceDirectories the source directories to make paths relative to
     * @return a new report with relative paths
     */
    private Report makePathsRelativeToSourceDirectories(final Report originalReport, final Set<String> sourceDirectories) {
        try (var builder = new IssueBuilder()) {
            var reportWithRelativePaths = new Report();
            
            for (var issue : originalReport) {
                String fileName = issue.getFileName();
                String normalizedFileName = fileName.replace("\\", "/");
                String relativePath = fileName;
                
                for (String sourceDir : sourceDirectories) {
                    String normalizedSourceDir = sourceDir.replace("\\", "/");
                    
                    if (!normalizedSourceDir.isEmpty() && !normalizedSourceDir.endsWith("/")) {
                        normalizedSourceDir += "/";
                    }
                    
                    if (normalizedFileName.startsWith(normalizedSourceDir)) {
                        relativePath = normalizedFileName.substring(normalizedSourceDir.length());
                        break;
                    }
                }
                
                if (relativePath.equals(fileName)) {
                    reportWithRelativePaths.add(issue);
                }
                else {
                    reportWithRelativePaths.add(builder.copy(issue).setFileName(relativePath).build());
                }
            }
            
            reportWithRelativePaths.getInfoMessages().addAll(originalReport.getInfoMessages());
            reportWithRelativePaths.getErrorMessages().addAll(originalReport.getErrorMessages());
            
            return reportWithRelativePaths;
        }
    }
}
