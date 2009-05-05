package hudson.plugins.warnings.util;

import java.io.Serializable;

import org.jfree.chart.urls.CategoryURLGenerator;

/**
 * A serializable {@link CategoryURLGenerator}.
 *
 * @author Ulli Hafner
 */
public interface SerializableUrlGenerator extends CategoryURLGenerator, Serializable {
    // empty
}

