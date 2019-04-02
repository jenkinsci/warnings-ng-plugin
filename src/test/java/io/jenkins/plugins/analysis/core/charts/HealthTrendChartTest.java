package io.jenkins.plugins.analysis.core.charts;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link HealthTrendChart}.
 *
 * @author Matthias Herpers
 */

public class HealthTrendChartTest {

    /**
     * Issue JENKINS-56708
     */

    @Test
    public void creatorTestWithNull()
    {
        HealthTrendChart hTC = new HealthTrendChart(null);
        hTC.create(null, null);
        Assert.assertTrue(true);
    }
}
