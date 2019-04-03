package io.jenkins.plugins.analysis.core.model.assertj;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import io.jenkins.plugins.analysis.core.model.TabLabelProvider;

/**
 * Assertions for {@link TabLabelProvider}.
 *
 * @author Tobias Redl
 */
@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion", "PMD.LinguisticNaming"})
public class TabLabelProviderAssert extends AbstractAssert<TabLabelProviderAssert, TabLabelProvider> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";

    /**
     * Creates a new {@link TabLabelProviderAssert} to make assertions on actual {@link TabLabelProvider}.
     *
     * @param actual
     *         the tabLabelProvider we want to make assertions on
     */
    TabLabelProviderAssert(final TabLabelProvider actual) {
        super(actual, TabLabelProviderAssert.class);
    }

    /**
     * Creates a new {@link TabLabelProviderAssert} to make assertions on actual {@link TabLabelProvider}.
     *
     * @param actual
     *         the tabLabelProvider we want to make assertions on
     *
     * @return a new {@link TabLabelProviderAssert}
     */
    public static TabLabelProviderAssert assertThat(final TabLabelProvider actual) {
        return new TabLabelProviderAssert(actual);
    }

    /**
     * Checks whether an TabLabelProvider has a specific packageName.
     *
     * @param packageName
     *         String specifying packageName.
     *
     * @return this
     */
    public TabLabelProviderAssert hasPackageName(final String packageName) {
        isNotNull();

        if (!Objects.equals(actual.getPackageName(), packageName)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "packageName", actual, packageName, actual.getPackageName());
        }
        return this;
    }

    /**
     * Checks whether an TabLabelProvider has specific packages.
     *
     * @param packages
     *         String specifying packages.
     *
     * @return this
     */
    public TabLabelProviderAssert hasPackages(final String packages) {
        isNotNull();

        if (!Objects.equals(actual.getPackages(), packages)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "packageName", actual, packages, actual.getPackages());
        }
        return this;
    }
}
