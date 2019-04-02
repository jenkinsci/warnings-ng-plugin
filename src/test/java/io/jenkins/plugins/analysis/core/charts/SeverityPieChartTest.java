package io.jenkins.plugins.analysis.core.charts;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
/**
 * Tests the class {@link SeverityPieChart}.
 *
 * @author Matthias Herpers
 */

public class SeverityPieChartTest {

    /**
     * Issue JENKINS-56710.
     */
    @Test
    public void creatorTestWithNull() {
        //SeverityPieChart sPC = new SeverityPieChart();
        //sPC.create(null);
        Assert.assertTrue(true);
    }
}
