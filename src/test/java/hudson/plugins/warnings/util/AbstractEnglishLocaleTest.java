package hudson.plugins.warnings.util;

import java.util.Locale;

import org.junit.Before;

/**
 * A test with predefined English locale.
 */
public abstract class AbstractEnglishLocaleTest {
    /**
     * Initializes the locale to English.
     */
    @Before
    public void initializeLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }
}

