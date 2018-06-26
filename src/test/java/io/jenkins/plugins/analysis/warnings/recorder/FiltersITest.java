package io.jenkins.plugins.analysis.warnings.recorder;

import javax.annotation.Nonnull;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Pmd;
import static org.assertj.core.api.Assertions.*;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TopLevelItem;

/**
 * Integration tests of the regex property filters.
 *
 * @author Manuel Hampp
 */
@SuppressWarnings("classdataabstractioncoupling")
public class FiltersITest extends IssuesRecorderITest {
    private static final String MODULE_FILTER = "module-filter/";

    /**
     * Tests the module expression filter: provides a pom.xml in the workspace so that modules are correctly assigned.
     */
    @Test
    public void shouldFilterPmdIssuesByModule() {
        Map<RegexpFilter, Integer[]> expectedLinesByFilter = setupModuleFilterForPmd();

        for (Entry<RegexpFilter, Integer[]> entry : expectedLinesByFilter.entrySet()) {
            FreeStyleProject project = createFreeStyleProject();
            copyAndExpandedVariables(project, MODULE_FILTER + "pmd.xml");
            copyMultipleFilesToWorkspace(project, MODULE_FILTER + "pom.xml",
                    MODULE_FILTER + "m1/pom.xml", MODULE_FILTER + "m2/pom.xml");
            enableWarnings(project, recorder -> recorder.setFilters(toFilter(entry)), new Pmd());

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
     * Validates the remaining issues in the projects result against the expected values.
     *
     * @param project
     *         project that contains the issues to compare
     * @param expectedValues
     *         issue line numbers that are expected
     */
    private void buildAndVerifyResults(final FreeStyleProject project, final Integer[] expectedValues) {
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getIssues()
                .stream()
                .map(Issue::getLineStart)
                .collect(Collectors.toList())).containsOnly(expectedValues);
    }

    private List<RegexpFilter> toFilter(final Entry<RegexpFilter, Integer[]> entry) {
        return Collections.singletonList(entry.getKey());
    }

    private void copyAndExpandedVariables(final TopLevelItem job, final String fileName) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            workspace.child(createWorkspaceFileName(fileName))
                    .copyFrom(
                            new ReplacingInputStream(asInputStream(fileName), "WORKSPACEDIRPLACEHOLDER".getBytes(),
                                    workspace.getRemote().getBytes()));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Simple filter for InputStream that replaces string parts with a given replacement. Mainly taken over from:
     * https://github.com/apache/poi/blob/trunk/src/java/org/apache/poi/util/ReplacingInputStream.java
     *
     * @author Manuel Hampp
     */
    class ReplacingInputStream extends FilterInputStream {
        private final LinkedList<Integer> input = new LinkedList<>();
        private final LinkedList<Integer> output = new LinkedList<>();
        private final byte[] search;
        private final byte[] replacement;

        ReplacingInputStream(final InputStream in, final byte[] search, final byte[] replacement) {
            super(in);
            this.search = search;
            this.replacement = replacement;
        }

        private boolean matchFound() {
            Iterator<Integer> inputIterator = input.iterator();
            for (byte b : search) {
                if (!inputIterator.hasNext() || b != inputIterator.next()) {
                    return false;
                }
            }
            return true;
        }

        private void readAhead() throws IOException {
            while (input.size() < search.length) {
                int next = super.read();
                input.offer(next);
                if (next == -1) {
                    break;
                }
            }
        }

        @Override
        public int read(@Nonnull final byte[] b) throws IOException {
            return super.read(b);
        }

        @Override
        public int read(@Nonnull final byte[] bytes, final int off, final int len) throws IOException {
            if (off < 0 || len < 0 || len > bytes.length - off) {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0) {
                return 0;
            }

            int c = read();
            if (c == -1) {
                return -1;
            }
            bytes[off] = (byte) c;

            int i = 1;
            try {
                for (; i < len; i++) {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    bytes[off + i] = (byte) c;
                }
            }
            catch (IOException ignored) {
                // ignore
            }
            return i;

        }

        @Override
        public int read() throws IOException {
            if (output.isEmpty()) {
                readAhead();
                if (matchFound()) {
                    for (byte aSearch : search) {
                        input.remove();
                    }
                    for (byte b : replacement) {
                        output.offer((int) b);
                    }
                }
                else {
                    output.add(input.remove());
                }
            }
            return output.remove();
        }
    }
}

