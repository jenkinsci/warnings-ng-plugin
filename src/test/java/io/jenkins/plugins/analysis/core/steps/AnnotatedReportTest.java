package io.jenkins.plugins.analysis.core.steps;

import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link AnnotatedReport}.
 *
 * @author Ullrich Hafner
 */
class AnnotatedReportTest {
//    @Test
//    void shouldStoreAllOrigins() {
//        Report report = new Report();
//        report.setId(ANALYSIS);
//        assertThat(report).hasId(ANALYSIS);
//        
//        assertThat(report.getSizeByOrigin()).isEmpty();
//
//        Report checkStyle = new Report();
//        checkStyle.setId(CHECKSTYLE);
//        Report findBugs = new Report();
//        findBugs.setId(FINDBUGS);
//        
//        report.addAll(checkStyle, findBugs);
//        assertThat(report.getSizeByOrigin()).containsOnly(entry(CHECKSTYLE, 0), entry(FINDBUGS, 0));
//
//        IssueBuilder builder = new IssueBuilder().setOrigin(FINDBUGS);
//        findBugs.add(builder.setLineStart(2).build());
//        findBugs.add(builder.setLineStart(3).build());
//        builder.setOrigin(CHECKSTYLE);
//        checkStyle.add(builder.setLineStart(1).build());
//        
//        report.addAll(checkStyle, findBugs);
//        assertThat(report.getSizeByOrigin()).containsOnly(entry(CHECKSTYLE, 1), entry(FINDBUGS, 2));
//
//
//        Report anotherCheckStyle = new Report();
//        anotherCheckStyle.setId(CHECKSTYLE);
//        anotherCheckStyle.add(builder.setLineStart(4).build());
//        anotherCheckStyle.add(builder.setLineStart(5).build());
//        anotherCheckStyle.add(builder.setLineStart(6).build());
//
//        report.addAll(anotherCheckStyle);
//        assertThat(report.getSizeByOrigin()).containsOnly(entry(CHECKSTYLE, 4), entry(FINDBUGS, 2));
//
//        Report onlyCheckStyle = report.filter(Issue.byOrigin(CHECKSTYLE));
//        assertThat(onlyCheckStyle).hasSize(4);
//        assertThat(onlyCheckStyle.getSizeByOrigin()).containsOnly(entry(CHECKSTYLE, 4), entry(FINDBUGS, 0));
//        
//        Report onlyOne = report.filter(issue -> issue.getLineStart() == 1);
//        assertThat(onlyOne).hasSize(1);
//        assertThat(onlyOne.getSizeByOrigin()).containsOnly(entry(CHECKSTYLE, 1), entry(FINDBUGS, 0));
//    }
//    

    /**
     * FIXME: write comment.
     */
    @Test
    void should() {
    }
}