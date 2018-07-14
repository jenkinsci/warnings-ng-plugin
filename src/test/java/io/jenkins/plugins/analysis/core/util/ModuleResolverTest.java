package io.jenkins.plugins.analysis.core.util;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.ModuleDetector;
import edu.hm.hafner.analysis.Report;
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
        Report report = new Report();
        IssueBuilder builder = new IssueBuilder();
        String fileName = "/file/with/warnings.txt";
        builder.setFileName(fileName);
        Issue noModule = builder.build();
        report.add(noModule);

        builder.setModuleName("module2");
        Issue withModule = builder.build();
        report.add(withModule);

        ModuleDetector detector = mock(ModuleDetector.class);
        when(detector.guessModuleName(fileName)).thenReturn("module1");

        ModuleResolver resolver = new ModuleResolver();
        resolver.run(report, detector);

        assertThat(report.get(0)).hasModuleName("module1");
        assertThat(report.get(1)).hasModuleName("module2");

        assertThat(report.getInfoMessages()).contains("-> resolved module names for 1 issues");
    }
}