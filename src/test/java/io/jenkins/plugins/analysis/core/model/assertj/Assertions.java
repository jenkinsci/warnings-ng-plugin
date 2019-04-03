package io.jenkins.plugins.analysis.core.model.assertj;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.analysis.core.model.TabLabelProvider;

/**
 * Custom assertions for {@link TabLabelProvider}.
 *
 * @author Tobias Redl
 */
@SuppressFBWarnings("NM")
public class Assertions extends org.assertj.core.api.Assertions {
    /**
     * Creates a new {@link TabLabelProviderAssert} to make assertions on actual {@link TabLabelProvider}.
     *
     * @param actual
     *         the tabLabelProvider we want to make assertions on
     *
     * @return a new {@link TabLabelProviderAssert}
     */
    public static TabLabelProviderAssert assertThat(final TabLabelProvider actual) {
        return new TabLabelProviderAssert(actual);
    }
}
