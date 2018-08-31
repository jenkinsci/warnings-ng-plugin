package io.jenkins.plugins.analysis.core.filter;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report.IssueFilterBuilder;
import io.jenkins.plugins.analysis.core.filter.IncludeType.DescriptorImpl;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter.RegexpFilterDescriptor;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link RegexpFilter}.
 *
 * @author Ullrich Hafner
 */
class RegexpFilterTest {
    private static final String PATTERN = "pattern";

    @Test
    void shouldValidatePattern() {
        RegexpFilterDescriptor descriptor = new DescriptorImpl();
        assertThat(descriptor.doCheckPattern(null)).isOk().hasMessage(Messages.pattern_blank());
        assertThat(descriptor.doCheckPattern(StringUtils.EMPTY)).isOk().hasMessage(Messages.pattern_blank());
        assertThat(descriptor.doCheckPattern("one brace (")).isError();
        assertThat(descriptor.doCheckPattern("backslash \\")).isError();

        assertThat(descriptor.doCheckPattern("^.*[a-z]")).isOk();
    }
    
    @Test
    void shouldCallIncludeCategoryMethod() {
        RegexpFilter filter = new IncludeCategory(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setIncludeCategoryFilter(PATTERN);
    }
    
    @Test
    void shouldCallIncludeTypeMethod() {
        RegexpFilter filter = new IncludeType(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setIncludeTypeFilter(PATTERN);
    }
    
    @Test
    void shouldCallIncludeFileMethod() {
        RegexpFilter filter = new IncludeFile(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setIncludeFileNameFilter(PATTERN);
    }
    
    @Test
    void shouldCallIncludePackageMethod() {
        RegexpFilter filter = new IncludePackage(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setIncludePackageNameFilter(PATTERN);
    }
    
    @Test
    void shouldCallIncludeModuleMethod() {
        RegexpFilter filter = new IncludeModule(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setIncludeModuleNameFilter(PATTERN);
    }
    
    @Test
    void shouldCallExcludeCategoryMethod() {
        RegexpFilter filter = new ExcludeCategory(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setExcludeCategoryFilter(PATTERN);
    }
    
    @Test
    void shouldCallExcludeTypeMethod() {
        RegexpFilter filter = new ExcludeType(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setExcludeTypeFilter(PATTERN);
    }
    
    @Test
    void shouldCallExcludeFileMethod() {
        RegexpFilter filter = new ExcludeFile(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setExcludeFileNameFilter(PATTERN);
    }
    
    @Test
    void shouldCallExcludePackageMethod() {
        RegexpFilter filter = new ExcludePackage(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setExcludePackageNameFilter(PATTERN);
    }
    
    @Test
    void shouldCallExcludeModuleMethod() {
        RegexpFilter filter = new ExcludeModule(PATTERN);

        IssueFilterBuilder filterBuilder = mock(IssueFilterBuilder.class);
        filter.apply(filterBuilder);
        
        verify(filterBuilder).setExcludeModuleNameFilter(PATTERN);
    }
    
}