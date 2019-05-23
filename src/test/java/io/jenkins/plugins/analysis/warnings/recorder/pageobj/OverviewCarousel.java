package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.hm.hafner.util.NoSuchElementException;

/**
 * Page Object for the Overview Carousel.
 *
 * @author Artem Polovyi
 */
public class OverviewCarousel extends CarouselPageObject {
    private static final String OVERVIEW_CAROUSEL_ID = "overview";

    /**
     * Creates a carousel page object for the overview pie charts.
     *
     * @param page
     *         the details view web page to get the carousel from
     */
    public OverviewCarousel(final HtmlPage page) {
        super(page, OVERVIEW_CAROUSEL_ID);
    }

    /**
     * Returns the type of the currently visible chart.
     *
     * @return the chart type that is currently visible
     */
    public PieChartType getActiveChartType() {
        return PieChartType.fromId(getActiveId());
    }

    /**
     * Returns all chart types that are available in the carousel.
     *
     * @return the available set of chart types
     */
    public SortedSet<PieChartType> getChartTypes() {
        return getDivs().stream()
                .map(DomElement::getId)
                .map(PieChartType::fromId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Defines the supported chart types.
     */
    public enum PieChartType {
        SEVERITIES, TREND;

        static PieChartType fromId(final String domId) {
            for (PieChartType type : values()) {
                if (convertDomIdToName(domId).equals(type.name())) {
                    return type;
                }
            }
            throw new NoSuchElementException("No such chart type found with div id '%s'", domId);
        }

        private static String convertDomIdToName(final String domId) {
            return domId.replaceAll("-chart", StringUtils.EMPTY)
                    .replace("-", "_")
                    .toUpperCase(Locale.ENGLISH);
        }
    }
}
