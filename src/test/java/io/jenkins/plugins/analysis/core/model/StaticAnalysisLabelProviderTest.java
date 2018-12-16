package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.CompositeLocalizable;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;

/**
 * Tests the class {@link StaticAnalysisLabelProvider}.
 *
 * @author Ullrich Hafner
 */
class StaticAnalysisLabelProviderTest {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String OTHER_NAME = "other";

    @Test
    void shouldReturnIdAndNameOfConstructorParametersInAllDisplayProperties() {
        StaticAnalysisLabelProvider labelProvider = new StaticAnalysisLabelProvider(ID, NAME);

        assertThat(labelProvider).hasId(ID);
        assertThat(labelProvider).hasName(NAME);
        assertThat(labelProvider.getLinkName()).contains(NAME);
        assertThat(labelProvider.getTrendName()).contains(NAME);

        labelProvider.setName(OTHER_NAME);
        assertThat(labelProvider).hasName(OTHER_NAME);
        assertThat(labelProvider.getLinkName()).contains(OTHER_NAME);
        assertThat(labelProvider.getTrendName()).contains(OTHER_NAME);
    }

    @Test
    void shouldReturnIdAndDefaultNameIfNoNameIsGiven() {
        StaticAnalysisLabelProvider emptyNameLabelProvider = new StaticAnalysisLabelProvider(ID, "");

        assertThat(emptyNameLabelProvider).hasId(ID);
        assertThat(emptyNameLabelProvider).hasName(emptyNameLabelProvider.getDefaultName());

        StaticAnalysisLabelProvider nullNameLabelProvider = new StaticAnalysisLabelProvider(ID, null);

        assertThat(nullNameLabelProvider).hasId(ID);
        assertThat(nullNameLabelProvider).hasName(nullNameLabelProvider.getDefaultName());

        StaticAnalysisLabelProvider noNameLabelProvider = new StaticAnalysisLabelProvider(ID);

        assertThat(noNameLabelProvider).hasId(ID);
        assertThat(noNameLabelProvider).hasName(noNameLabelProvider.getDefaultName());
    }

    /**
     * Tests the class {@link AgeBuilder}.
     */
    @Nested
    class AgeBuilderTest {
        @Test
        void shouldCreateAgeLinkForFirstBuild() {
            AgeBuilder builder = new DefaultAgeBuilder(1, "checkstyle/");

            assertThat(builder.apply(1)).isEqualTo("1");
        }

        @Test
        void shouldCreateAgeLinkForPreviousBuilds() {
            AgeBuilder builder = new DefaultAgeBuilder(10, "checkstyle/");
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../1/checkstyle\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../9/checkstyle\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }

        @Test
        void shouldCreateAgeLinkForSubDetails() {
            AgeBuilder builder = new DefaultAgeBuilder(10, "checkstyle/package.1234/");
            assertThat(builder.apply(1))
                    .isEqualTo("<a href=\"../../../1/checkstyle\">10</a>");
            assertThat(builder.apply(9))
                    .isEqualTo("<a href=\"../../../9/checkstyle\">2</a>");
            assertThat(builder.apply(10))
                    .isEqualTo("1");
        }
    }

    /**
     * Tests the utility class {@link CompositeLocalizable}.
     */
    @Nested
    class CompositeLocalizableTest {
        @Test
        void shouldCreateComposedMessage() {
            CompositeLocalizable localizable = new CompositeLocalizable("Static Analysis", Messages._Tool_NoIssues());

            assertThat(localizable).hasToString("Static Analysis: No warnings");
            assertThat(localizable.toString(Locale.ENGLISH)).isEqualTo("Static Analysis: No warnings");
            assertThat(localizable.getKey()).isEqualTo(Messages._Tool_NoIssues().getKey());
        }
    }
}