package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ColumnMarkerTest {
    @Test
    void withColumnStartZeroThenDontMark() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 0, 0))
                .contains("text that could be code");
    }
    @Test
    void givenColumnStartAndColumnEndZeroThenMarkFromStartToLineEnd() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 6, 0))
                .contains("text OpEnMARKthat could be codeClOsEMARK");
    }
    @Test
    void givenColumnStartAndColumnEndwithColumnEndPointingToLineEndThenMarkFromStartToLineEnd() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 6, 23))
                .contains("text OpEnMARKthat could be codeClOsEMARK");
    }
    @Test
    void givenColumnStartAndColumnEndThenMarkFromColumnStartToColumnEnd() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 6, 10))
                .contains("text OpEnMARKthat ClOsEMARKcould be code");
    }
    @Test
    void givenColumnStartAndColumnEndWithDifferenceOfOneThenMarkFromColumnStartToColumnEnd() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 6, 7))
                .contains("text OpEnMARKthClOsEMARKat could be code");
    }
    @Test
    void givenColumnStartAndColumnEndWithSameValueThenMarkOneCharacter() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 6, 6))
                .contains("text OpEnMARKtClOsEMARKhat could be code");
    }
    @Test
    void givenAnEmptyTextThenMarkNothing() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("", 6, 6))
                .contains("");
    }

    @Test
    void givenColumnStartWithValueOneThenMarkTheLineFromBegin() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 1, 6))
                .contains("OpEnMARKtext tClOsEMARKhat could be code");
    }
    @Test
    void givenColumnStartWithValueOfTheLastCharacterThenMarkTheLastCharacter() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 23, 0))
                .contains("text that could be codOpEnMARKeClOsEMARK");
    }
    @Test
    void givenColumnStartWithValueOfBehindColumnEndThenDoNotMark() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 23, 10))
                .contains("text that could be code");
    }
    @Test
    void givenColumnStartIsAfterLineEndThenDoNotMark() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 30, 10))
                .contains("text that could be code");
    }
    @Test
    void givenColumnStartIsNegativeThenDoNotMark() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", -1, 10))
                .contains("text that could be code");
    }
    @Test
    void givenColumnEndIsNegativeThenDoNotMark() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 1, -1))
                .contains("text that could be code");
    }
    @Test
    void givenColumnEndIsAfterLineEndThenDoNotMark() {
        assertThat(new ColumnMarker("MARK")
                .markColumns("text that could be code", 1, 24))
                .contains("text that could be code");
    }
}