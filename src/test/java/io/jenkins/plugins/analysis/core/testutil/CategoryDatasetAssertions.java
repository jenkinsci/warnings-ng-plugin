package io.jenkins.plugins.analysis.core.testutil;

import java.util.List;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Lists;
import org.jfree.data.category.CategoryDataset;

/**
 * Assertions for {@link CategoryDataset} instances.
 *
 * @author Florian Pirchmoser
 */
public class CategoryDatasetAssertions extends AbstractAssert<CategoryDatasetAssertions, CategoryDataset> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";

    CategoryDatasetAssertions(final CategoryDataset o) {
        super(o, CategoryDatasetAssertions.class);
    }

    /**
     * Assert that the values of the dataset match the expectation exactly.
     *
     * @param expectedValues
     *         expected values in dataset
     *
     * @return this
     */
    public final CategoryDatasetAssertions containsExactly(final List<List<Integer>> expectedValues) {
        List<List<Integer>> actualValues = Lists.newArrayList();
        for (int c = 0; c < this.actual.getColumnCount(); ++c) {
            List<Integer> rowValues = Lists.newArrayList();
            for (int r = 0; r < this.actual.getRowCount(); ++r) {
                rowValues.add(this.actual.getValue(r, c).intValue());
            }
            actualValues.add(rowValues);
        }

        if (!Objects.equals(actualValues, expectedValues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "values", this.actual.getClass().getSimpleName(), expectedValues, actualValues);
        }

        return this;
    }
}
