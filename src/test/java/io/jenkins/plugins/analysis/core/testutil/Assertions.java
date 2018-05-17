package io.jenkins.plugins.analysis.core.testutil;

import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.views.IssuesDetail;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import net.sf.json.JSONArray;

import hudson.util.FormValidation;

/**
 * Custom assertions for {@link FormValidation} instances.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("NM")
public class Assertions extends edu.hm.hafner.analysis.assertj.Assertions {
    /**
     * An entry point for {@link CategoryDatasetAssertions} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(dataSet)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the data set we want to make assertions on
     *
     * @return a new {@link CategoryDatasetAssertions}
     */
    public static CategoryDatasetAssertions assertThat(final CategoryDataset actual) {
        return new CategoryDatasetAssertions(actual);
    }

    /**
     * An entry point for {@link IssuesDetailAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(myIssues)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link IssuesDetailAssert}
     */
    public static IssuesDetailAssert assertThat(final IssuesDetail actual) {
        return new IssuesDetailAssert(actual);
    }

    /**
     * An entry point for {@link FormValidationAssert} to follow AssertJ standard {@code assertThat()}. With a static
     * import, one can write directly {@code assertThat(formValidation)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link FormValidationAssert}
     */
    public static FormValidationAssert assertThat(final FormValidation actual) {
        return new FormValidationAssert(actual);
    }

    /**
     * An entry point for {@link JsonFluentAssert} to follow AssertJ standard {@code assertThat()}. With a static
     * import, one can write directly {@code assertThat(jsonObject)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link JsonFluentAssert}
     */

    public static JsonFluentAssert assertThatJson(final Object actual) {
        return JsonFluentAssert.assertThatJson(actual);
    }

    /**
     * An entry point for {@link JsonFluentAssert} to follow AssertJ standard {@code assertThat()}. With a static
     * import, one can write directly {@code assertThat(jsonObject)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link JsonFluentAssert}
     */

    public static JsonFluentAssert assertThat(final JSONArray actual) {
        return JsonFluentAssert.assertThatJson(actual);
    }

}
