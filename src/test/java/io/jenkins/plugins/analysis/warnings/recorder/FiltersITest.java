package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Test;

import edu.hm.hafner.analysis.Issue;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ExcludeCategory;
import io.jenkins.plugins.analysis.core.model.ExcludeFile;
import io.jenkins.plugins.analysis.core.model.ExcludeModule;
import io.jenkins.plugins.analysis.core.model.ExcludePackage;
import io.jenkins.plugins.analysis.core.model.ExcludeType;
import io.jenkins.plugins.analysis.core.model.IncludeCategory;
import io.jenkins.plugins.analysis.core.model.IncludeFile;
import io.jenkins.plugins.analysis.core.model.IncludeModule;
import io.jenkins.plugins.analysis.core.model.IncludePackage;
import io.jenkins.plugins.analysis.core.model.IncludeType;
import io.jenkins.plugins.analysis.core.model.RegexpFilter;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Pmd;
import static org.assertj.core.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration tests of the regex property filters.
 *
 * @author Manuel Hampp
 */
@SuppressWarnings("classdataabstractioncoupling")
public class FiltersITest extends AbstractIssuesRecorderITest {
    private static final String MODULE_FILTER = "module-filter/";

    /**
     * Tests the module expression filter: provides a pom.xml in the workspace so that modules are correctly assigned.
     */
    @Test
    public void shouldFilterPmdIssuesByModule() {
        Map<RegexpFilter, Integer[]> expectedLinesByFilter = setupModuleFilterForPmd();

        for (Entry<RegexpFilter, Integer[]> entry : expectedLinesByFilter.entrySet()) {
            FreeStyleProject project = createFreeStyleProject();
            copyDirectoryToWorkspace(project, MODULE_FILTER);
            enableWarnings(project, recorder -> recorder.setFilters(toFilter(entry)),
                    new ToolConfiguration(new Pmd(), "**/pmd.xml"));

            buildAndVerifyResults(project, entry.getValue());
        }
    }

    /**
     * Provides a map, that contains the filters and the line numbers that are expected to remain after filtering.
     */
    private Map<RegexpFilter, Integer[]> setupModuleFilterForPmd() {
        /*
        CopyToClipboard.java:54         com.avaloq.adt.env.internal.ui.actions          Basic CollapsibleIfStatements   Normal  1
        ChangeSelectionAction.java:14   com.avaloq.adt.env.internal.ui.actions.change   Import Statement Rules  UnusedImports   Normal  1
         */
        Map<RegexpFilter, Integer[]> filterResultMap = new HashMap<>();
        filterResultMap.put(new RegexpFilter("m1", new ExcludeModule()), new Integer[]{14});
        filterResultMap.put(new RegexpFilter("m1", new IncludeModule()), new Integer[]{54});

        return filterResultMap;
    }

    /**
     * Tests the category and file expression filter by comparing the result with expected.
     */
    @Test
    public void shouldFilterCheckStyleIssuesByCategoryAndFile() {
        Map<RegexpFilter, Integer[]> expectedLinesByFilter = setupCategoryFilterForCheckStyle();

        for (Entry<RegexpFilter, Integer[]> entry : expectedLinesByFilter.entrySet()) {
            FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle-filtering.xml");
            enableWarnings(project, recorder -> recorder.setFilters(toFilter(entry)), new CheckStyle());

            buildAndVerifyResults(project, entry.getValue());
        }
    }

    /**
     * Provides a map, that contains the filters and the line numbers that are expected to remain after filtering.
     */
    private Map<RegexpFilter, Integer[]> setupCategoryFilterForCheckStyle() {
        /*
          CsharpNamespaceDetector.java:30   -Blocks RightCurlyCheck High 1
          CsharpNamespaceDetector.java:37   -Blocks RightCurlyCheck High 1
          CsharpNamespaceDetector.java:17   -Design DesignForExtensionCheck High 1
          CsharpNamespaceDetector.java:22   -Design DesignForExtensionCheck High 1
          CsharpNamespaceDetector.java:42   -Sizes LineLengthCheck High 1
          CsharpNamespaceDetector.java:29   -Sizes LineLengthCheck High 1
          FileFinder.java:99                -Blocks RightCurlyCheck High 1
         */
        Map<RegexpFilter, Integer[]> filterResultMap = new HashMap<>();
        filterResultMap.put(new RegexpFilter("Blocks", new IncludeCategory()), new Integer[]{30, 37, 99});
        filterResultMap.put(new RegexpFilter("Blocks", new ExcludeCategory()), new Integer[]{17, 22, 42, 29});
        filterResultMap.put(new RegexpFilter("(Blocks|Design)", new ExcludeCategory()), new Integer[]{42, 29});
        filterResultMap.put(new RegexpFilter("(Blocks|Design)", new IncludeCategory()),
                new Integer[]{30, 37, 17, 22, 99});
        filterResultMap.put(new RegexpFilter(".*Csharp.*", new ExcludeFile()), new Integer[]{99});
        filterResultMap.put(new RegexpFilter(".*Csharp.*", new IncludeFile()), new Integer[]{30, 37, 17, 22, 42, 29});

        return filterResultMap;
    }

    /**
     * Tests the package and type expression filter by comparing the result with expected.
     */
    @Test
    public void shouldFilterPmdIssuesByPackageAndType() {
        Map<RegexpFilter, Integer[]> typeFiltersWithResult = setupCategoryFilterForPmd();

        for (Entry<RegexpFilter, Integer[]> entry : typeFiltersWithResult.entrySet()) {
            FreeStyleProject project = createJobWithWorkspaceFiles("pmd-warnings.xml");
            enableWarnings(project, recorder -> recorder.setFilters(toFilter(entry)), new Pmd());

            buildAndVerifyResults(project, entry.getValue());
        }
    }

    /**
     * Provides a map, that contains the filters and the line numbers that are expected to remain after filtering.
     */
    private Map<RegexpFilter, Integer[]> setupCategoryFilterForPmd() {
        /*
        CopyToClipboard.java:54         com.avaloq.adt.env.internal.ui.actions          Basic CollapsibleIfStatements   Normal  1
        ChangeSelectionAction.java:14   com.avaloq.adt.env.internal.ui.actions.change   Import Statement Rules  UnusedImports   Normal  1
        SelectSourceDialog.java:938     com.avaloq.adt.env.internal.ui.dialogs          Basic Rules EmptyCatchBlock Low 1
        SelectSourceDialog.java:980     com.avaloq.adt.env.internal.ui.dialogs          Basic Rules EmptyCatchBlock High    1
         */
        HashMap<RegexpFilter, Integer[]> filterResultMap = new HashMap<>();
        filterResultMap.put(new RegexpFilter(".*actions", new IncludePackage()), new Integer[]{54});
        filterResultMap.put(new RegexpFilter(".*actions.*", new IncludePackage()), new Integer[]{54, 14});
        filterResultMap.put(new RegexpFilter(".*actions.*", new ExcludePackage()), new Integer[]{938, 980});
        filterResultMap.put(new RegexpFilter(".*actions", new ExcludePackage()), new Integer[]{14, 938, 980});

        filterResultMap.put(new RegexpFilter(".*EmptyCatchBlock", new IncludeType()), new Integer[]{938, 980});
        filterResultMap.put(new RegexpFilter(".*EmptyCatchBlock", new ExcludeType()), new Integer[]{54, 14});

        return filterResultMap;
    }

    /**
     * Validates the filtered issues in the projects. Asserts that only issues with the specified lines are retained.
     *
     * @param project
     *         project that contains the issues to compare
     * @param expectedLines
     *         issue line numbers that are expected
     */
    private void buildAndVerifyResults(final FreeStyleProject project, final Integer[] expectedLines) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(getLines(result)).containsOnly(expectedLines);
    }

    private List<Integer> getLines(final AnalysisResult result) {
        return result.getIssues()
                .stream()
                .map(Issue::getLineStart)
                .collect(Collectors.toList());
    }

    private List<RegexpFilter> toFilter(final Entry<RegexpFilter, Integer[]> entry) {
        return Collections.singletonList(entry.getKey());
    }
}

