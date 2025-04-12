package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link TabLabelProvider}.
 *
 * @author Tobias Redl
 */
class TabLabelProviderTest {
    private TabLabelProvider createTabLabelProvider(final String fileName) {
        Issue issue = mock(Issue.class);
        when(issue.getFileName()).thenReturn(fileName);

        var report = new Report();
        report.add(issue);

        return new TabLabelProvider(report);
    }

    @Test
    void shouldReturnPackageNameJava() {
        var tabLabelProvider = createTabLabelProvider("Testfile.java");

        assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Package());
    }

    @Test
    void shouldReturnPackageNamePython() {
        var tabLabelProvider = createTabLabelProvider("Testfile.py");

        assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Package());
    }

    @Test
    void shouldReturnPackageNameCS() {
        var tabLabelProvider = createTabLabelProvider("Testfile.cs");

        assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Namespace());
    }

    @Test
    void shouldReturnPackageNameFallback() {
        var tabLabelProvider = createTabLabelProvider("Test.file");

        assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Folder());
    }

    @Test
    void shouldReturnPackagesJava() {
        var tabLabelProvider = createTabLabelProvider("Testfile.java");

        assertThat(tabLabelProvider).hasPackages(Messages.Tab_Packages());
    }

    @Test
    void shouldReturnPackagesPython() {
        var tabLabelProvider = createTabLabelProvider("Testfile.py");

        assertThat(tabLabelProvider).hasPackages(Messages.Tab_Packages());
    }

    @Test
    void shouldReturnPackagesCS() {
        var tabLabelProvider = createTabLabelProvider("Testfile.cs");

        assertThat(tabLabelProvider).hasPackages(Messages.Tab_Namespaces());
    }

    @Test
    void shouldReturnPackagesFallback() {
        var tabLabelProvider = createTabLabelProvider("Test.file");

        assertThat(tabLabelProvider).hasPackages(Messages.Tab_Folders());
    }
}
