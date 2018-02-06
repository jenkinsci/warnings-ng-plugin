package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.util.Ensure;
import static io.jenkins.plugins.analysis.core.views.IssuesDetail.*;
import io.jenkins.plugins.analysis.core.views.LocalizedPriority;

import hudson.util.RobustCollectionConverter;
import hudson.util.XStream2;

/**
 * Adds some Jenkins properties to an issue. Note that instances of this class use the {@link #equals(Object)} and
 * {@link #hashCode()} methods from the parent.
 *
 * @author Ullrich Hafner
 */
// FIXME: destroys CodeDuplications, should have a reference
public class BuildIssue extends Issue {
    private final int build;

    public BuildIssue(final Issue issue, final int build) {
        super(issue);

        this.build = build;
    }

    public int getBuild() {
        return build;
    }

    /**
     * Creates a new {@link XStream2} to serialize {@link BuildIssue} instances.
     *
     * @return the stream
     */
    public static XStream2 createStream() {
        XStream2 xStream2 = new XStream2();
        xStream2.registerConverter(new LineRangeListConverter(xStream2));
        xStream2.alias("lineRange", LineRange.class);
        xStream2.alias("issue", BuildIssue.class);
        return xStream2;
    }

    // FIXME: should this return the JSON object?
    public String toJson() {
        return String.format("[\"%s\", \"%s\", \"%s\", \"%s\", \"%s\"]",
                formatFileName(),
                formatProperty("packageName", getPackageName()),
                formatProperty("category", getCategory()),
                formatProperty("type", getType()),
                formatPriority());
    }

    private String formatPriority() {
        return String.format("<a href=\\\"%s\\"
                        + "\">%s</a>", getPriority().name(),
                LocalizedPriority.getLocalizedString(getPriority()));
    }

    private String formatProperty(final String property, final String value) {
        return String.format("<a href=\\\"%s.%d/\\\">%s</a>", property, value.hashCode(), value);
    }

    private String formatFileName() {
        return String.format("<a href=\\\"source.%s/#%d\\\">%s:%d<\\"
                        + "/a>", getId(), getLineStart(),
                FILE_NAME_FORMATTER.apply(getFileName()), getLineStart());
    }

    /**
     * {@link Converter} implementation for XStream.
     */
    @SuppressWarnings("rawtypes")
    public static final class LineRangeListConverter extends RobustCollectionConverter {
        public LineRangeListConverter(final XStream xs) {
            super(xs);
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == LineRangeList.class;
        }

        @SuppressWarnings("CastToConcreteClass")
        @Override
        protected void populateCollection(final HierarchicalStreamReader reader,
                final UnmarshallingContext context, final Collection collection) {
            super.populateCollection(reader, context, collection);
            Ensure.that(collection).isInstanceOf(LineRangeList.class);
            ((LineRangeList) collection).trim();
        }

        @Override
        protected Object createCollection(final Class type) {
            return new LineRangeList();
        }
    }
}
