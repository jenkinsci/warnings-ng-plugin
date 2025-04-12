package io.jenkins.plugins.analysis.core.testutil;

import org.junit.jupiter.api.BeforeAll;

import java.util.Locale;

/**
 * Base class for tests that verify localized messages. Sets the default locale to {@link Locale#ENGLISH} so that the
 * results do not depend on the locale of the system that runs the tests.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class LocalizedMessagesTest {
    @BeforeAll
    static void initializeLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }
}
