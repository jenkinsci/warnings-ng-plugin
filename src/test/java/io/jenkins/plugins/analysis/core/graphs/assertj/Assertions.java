package io.jenkins.plugins.analysis.core.graphs.assertj;

import org.jfree.data.category.CategoryDataset;

public class Assertions {

    /**
     * Factory method for custom category dataset assertions.
     *
     * @param actual
     *         value to run assertions on
     *
     * @return new instance of assertions class
     */
    public static CategoryDatasetAssertions assertThat(final CategoryDataset actual) {
        return new CategoryDatasetAssertions(actual);
    }
}
