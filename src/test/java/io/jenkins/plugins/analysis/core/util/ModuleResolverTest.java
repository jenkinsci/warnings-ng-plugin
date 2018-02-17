package io.jenkins.plugins.analysis.core.util;

import java.io.File;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.ModuleDetector;
import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ModuleResolver}.
 *
 * @author Ullrich Hafner
 */
class ModuleResolverTest {
    @Test
    void shouldAssignModuleName() {
        Issues<Issue> issues = new Issues<>();
        IssueBuilder builder = new IssueBuilder();
        String fileName = "/file/with/warnings.txt";
        builder.setFileName(fileName);
        Issue noModule = builder.build();
        issues.add(noModule);

        builder.setModuleName("module2");
        Issue withModule = builder.build();
        issues.add(withModule);

        File workspace = mock(File.class);
        ModuleDetector detector = mock(ModuleDetector.class);
        when(detector.guessModuleName(fileName)).thenReturn("module1");

        ModuleResolver resolver = new ModuleResolver();
        resolver.run(issues, workspace, detector);

        assertThat(issues.get(0)).hasModuleName("module1");
        assertThat(issues.get(1)).hasModuleName("module2");

        assertThat(issues.getInfoMessages()).contains("Resolved module names for 1 issues");
    }
}