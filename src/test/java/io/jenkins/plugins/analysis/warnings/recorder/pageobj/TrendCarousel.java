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
 * Page Object for the trend carousel in details views.
 */
public class TrendCarousel extends CarouselPageObject {
    private static final String TREND_CAROUSEL_ID = "trend";

    /**
     * Creates a carousel page object for the trend charts.
     *
     * @param page
     *         the details view web page to get the carousel from
     */
    public TrendCarousel(final HtmlPage page) {
        super(page, TREND_CAROUSEL_ID);
    }

    /**
     * Returns the type of the currently visible chart.
     *
     * @return the chart type that is currently visible
     */
    public TrendChartType getActiveChartType() {
        return TrendChartType.fromId(getActiveId());
    }

    /**
     * Returns all chart types that are available in the carousel.
     *
     * @return the available set of chart types
     */
    public SortedSet<TrendChartType> getChartTypes() {
        return getDivs().stream()
                .map(DomElement::getId)
                .map(TrendChartType::fromId)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Defines the supported chart types.
     */
    public enum TrendChartType {
        SEVERITIES, TOOLS, NEW_VERSUS_FIXED, HEALTH;

        static TrendChartType fromId(final String domId) {
            for (TrendChartType type : values()) {
                if (convertDomIdToName(domId).equals(type.name())) {
                    return type;
                }
            }
            throw new NoSuchElementException("No such chart type found with div id '%s'", domId);
        }

        private static String convertDomIdToName(final String domId) {
            return domId.replaceAll("-trend-chart", StringUtils.EMPTY)
                    .replace("-", "_")
                    .toUpperCase(Locale.ENGLISH);
        }
    }
}
