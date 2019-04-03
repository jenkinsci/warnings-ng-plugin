package io.jenkins.plugins.analysis.core.model.assertj;

import java.util.Iterator;

import org.assertj.core.api.AbstractStandardSoftAssertions;
import org.assertj.core.api.IterableAssert;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import io.jenkins.plugins.analysis.core.model.TabLabelProvider;

/**
 * Custom soft assertions for {@link TabLabelProvider}.
 *
 * @author Tobias Redl
 */
public class SoftAssertions extends AbstractStandardSoftAssertions {
    /**
     * Creates a new {@link TabLabelProviderAssert} to make assertions on actual {@link TabLabelProvider}.
     *
     * @param actual
     *         the tabLabelProvider we want to make assertions on
     *
     * @return a new {@link TabLabelProviderAssert}
     */
    public TabLabelProviderAssert assertThat(final TabLabelProvider actual) {
        return proxy(TabLabelProviderAssert.class, TabLabelProvider.class, actual);
    }

    /**
     * An entry point for {@link ImmutableSortedSet} to follow AssertJ standard {@code assertThat()}. With a static
     * import, one can write directly {@code assertThat(set)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the tabLabelProvider we want to make assertions on
     * @param <T>
     *         type of the collection elements
     *
     * @return a new {@link IterableAssert}
     */
    @SuppressWarnings("unchecked")
    public <T> IterableAssert<T> assertThat(final ImmutableSortedSet<T> actual) {
        return proxy(IterableAssert.class, Iterator.class, actual.iterator());
    }
}
