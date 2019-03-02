package io.jenkins.plugins.analysis.core.charts;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.impl.multimap.list.FastListMultimap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies that a multi map is capable of storing duplicate key-value pairs.
 */
class MultiMapTest {
    @Test
    void shouldStoreDuplicatesInMultiMap() {
        FastListMultimap<LocalDate, List<Integer>> valuesPerDate = FastListMultimap.newMultimap();
        valuesPerDate.put(LocalDate.now(), asList(1));
        valuesPerDate.put(LocalDate.now(), asList(1));
        valuesPerDate.put(LocalDate.now(), asList(2));
        valuesPerDate.put(LocalDate.now(), asList(2));
        valuesPerDate.put(LocalDate.now(), asList(3));
        valuesPerDate.put(LocalDate.now(), asList(3));

        assertThat(valuesPerDate.keySet()).hasSize(1);
        assertThat(valuesPerDate.valuesView()).hasSize(6);
    }

    private List<Integer> asList(final int value) {
        return Collections.singletonList(value);
    }
}
