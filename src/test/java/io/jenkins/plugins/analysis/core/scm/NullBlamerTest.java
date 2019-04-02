package io.jenkins.plugins.analysis.core.scm;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link NullBlamer}
 *
 * @author Florian Hageneder
 */
class NullBlamerTest {

    @Test
    void shouldLogSkippedBlaming() {
        Report report = mock(Report.class);

        final NullBlamer blamer = new NullBlamer();
        blamer.blame(report);

        verify(report).logInfo(anyString(), any());
    }

    @Test
    void shouldReturnEmptyBlame() {
        final Report report = mock(Report.class);

        final Blames empty = new NullBlamer().blame(report);

        assertThat(empty).isEmpty();
        assertThat(empty.size()).isEqualTo(0);
    }

}
