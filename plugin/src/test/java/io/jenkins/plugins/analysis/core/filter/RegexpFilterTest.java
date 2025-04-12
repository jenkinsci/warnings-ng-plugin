package io.jenkins.plugins.analysis.core.filter;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueFilterBuilder;

import hudson.model.BuildableItem;
import hudson.model.Item;

import io.jenkins.plugins.analysis.core.filter.IncludeType.DescriptorImpl;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link RegexpFilter}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("ClassDataAbstractionCoupling")
class RegexpFilterTest {
    private static final String PATTERN = "pattern";
    private static final IssueBuilder ISSUE_BUILDER = new IssueBuilder();

    @Test
    void issue54035() {
        var report = new Report();
        report.add(ISSUE_BUILDER.setFileName("warning.txt").build());
        report.add(ISSUE_BUILDER.setFileName("_build.external/mercury/Testing/na/na_test.c").build());
        report.add(ISSUE_BUILDER.setFileName("@2/_build.external/pmix/src/mca/gds/gds.h").build());

        var filter = new ExcludeFile(".*_build\\.external\\/.*");

        var builder = new IssueFilterBuilder();
        filter.apply(builder);

        var filtered = report.filter(builder.build());

        assertThat(filtered).hasSize(1);
        assertThat(report.get(0)).hasFileName("warning.txt");
    }

    @Test
    void shouldValidatePattern() {
        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.hasPermission(Item.CONFIGURE, (BuildableItem) null)).thenReturn(true);

        var descriptor = new DescriptorImpl(jenkinsFacade);
        assertThat(descriptor.doCheckPattern(null, null)).isOk().hasMessage(Messages.pattern_blank());
        assertThat(descriptor.doCheckPattern(null, StringUtils.EMPTY)).isOk().hasMessage(Messages.pattern_blank());
        assertThat(descriptor.doCheckPattern(null, "one brace (")).isError();
        assertThat(descriptor.doCheckPattern(null, "backslash \\")).isError();

        assertThat(descriptor.doCheckPattern(null, "^.*[a-z]")).isOk();
    }

    @Test
    void shouldCallIncludeCategoryMethod() {
        var filter = new IncludeCategory(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setIncludeCategoryFilter(PATTERN);
    }

    @Test
    void shouldCallIncludeTypeMethod() {
        var filter = new IncludeType(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setIncludeTypeFilter(PATTERN);
    }

    @Test
    void shouldCallIncludeFileMethod() {
        var filter = new IncludeFile(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setIncludeFileNameFilter(PATTERN);
    }

    @Test
    void shouldCallIncludePackageMethod() {
        var filter = new IncludePackage(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setIncludePackageNameFilter(PATTERN);
    }

    @Test
    void shouldCallIncludeModuleMethod() {
        var filter = new IncludeModule(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setIncludeModuleNameFilter(PATTERN);
    }

    @Test
    void shouldCallIncludeMessageMethod() {
        var filter = new IncludeMessage(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setIncludeMessageFilter(PATTERN);
    }

    @Test
    void shouldCallExcludeCategoryMethod() {
        var filter = new ExcludeCategory(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setExcludeCategoryFilter(PATTERN);
    }

    @Test
    void shouldCallExcludeTypeMethod() {
        var filter = new ExcludeType(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setExcludeTypeFilter(PATTERN);
    }

    @Test
    void shouldCallExcludeFileMethod() {
        var filter = new ExcludeFile(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setExcludeFileNameFilter(PATTERN);
    }

    @Test
    void shouldCallExcludePackageMethod() {
        var filter = new ExcludePackage(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setExcludePackageNameFilter(PATTERN);
    }

    @Test
    void shouldCallExcludeModuleMethod() {
        var filter = new ExcludeModule(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setExcludeModuleNameFilter(PATTERN);
    }

    @Test
    void shouldCallExcludeMessageMethod() {
        var filter = new ExcludeMessage(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);

        verify(filterBuilder).setExcludeMessageFilter(PATTERN);
    }
}
