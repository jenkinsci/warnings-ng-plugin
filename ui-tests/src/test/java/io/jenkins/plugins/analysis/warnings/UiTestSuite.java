package io.jenkins.plugins.analysis.warnings;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite of all non docker based UI tests.
 *
 * @author Ullrich Hafner
 */
@RunWith(Suite.class)
@SuiteClasses({
        DashboardViewPortletUiTest.class,
        DetailsTabUiTest.class,
        FreeStyleConfigurationUiTest.class,
        GitBlamerAndForensicsUiTest.class,
        GlobalConfigurationUiTest.class,
        IssuesColumnUiTest.class,
        SnippetGeneratorUiTest.class,
        TrendChartsUiTest.class,
        WarningsPluginUiTest.class})
public class UiTestSuite {
}
