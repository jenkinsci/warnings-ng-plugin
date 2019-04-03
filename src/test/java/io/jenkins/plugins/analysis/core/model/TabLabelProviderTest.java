package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import static org.mockito.Mockito.*;

/**
 * Tests the class {@link TabLabelProvider}.
 *
 * @author Tobias Redl
 */
public class TabLabelProviderTest {

    private TabLabelProvider createTabLabelProvider(final String fileName) {
        Issue issue = mock(Issue.class);
        when(issue.getFileName()).thenReturn(fileName);

        Report report = new Report();
        report.add(issue);

        return new TabLabelProvider(report);
    }

    @Test
    void shouldReturnPackageNameJava() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Testfile.java");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Package());
    }

    @Test
    void shouldReturnPackageNamePython() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Testfile.py");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Package());
    }

    @Test
    void shouldReturnPackageNameCS() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Testfile.cs");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Namespace());
    }

    @Test
    void shouldReturnPackageNameFallback() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Test.file");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackageName(Messages.Tab_Folder());
    }

    @Test
    void shouldReturnPackagesJava() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Testfile.java");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackages(Messages.Tab_Packages());

    }

    @Test
    void shouldReturnPackagesPython() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Testfile.py");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackages(Messages.Tab_Packages());
    }

    @Test
    void shouldReturnPackagesCS() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Testfile.cs");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackages(Messages.Tab_Namespaces());
    }

    @Test
    void shouldReturnPackagesFallback() {
        TabLabelProvider tabLabelProvider = createTabLabelProvider("Test.file");

        TabLabelProviderAssert.assertThat(tabLabelProvider).hasPackages(Messages.Tab_Folders());
    }
}
