package io.jenkins.plugins.analysis.core.scm;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link NullBlamer}.
 *
 * @author Florian Hageneder
 */
class NullBlamerTest {

    @Test
    void shouldLogSkippedBlaming() {
        Report report = new Report();

        final NullBlamer blamer = new NullBlamer();
        blamer.blame(report);

        assertThat(report.getInfoMessages()).contains(NullBlamer.BLAMING_SKIPPED);
    }

    @Test
    void shouldReturnEmptyBlame() {
        Report report = mock(Report.class);

        Blames empty = new NullBlamer().blame(report);

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
    }

}
