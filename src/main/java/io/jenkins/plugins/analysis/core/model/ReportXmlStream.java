package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.LineRange;
import edu.hm.hafner.analysis.LineRangeList;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.Ensure;
import edu.hm.hafner.util.TreeString;
import edu.hm.hafner.util.TreeStringBuilder;

import hudson.util.RobustCollectionConverter;
import hudson.util.XStream2;

/**
 * Reads {@link Issue issues} from an XML file.
 *
 * @author Ullrich Hafner
 */
 class ReportXmlStream extends AbstractXmlStream<Report> {
    ReportXmlStream() {
        super(Report.class);
    }

    @Override
    Report createDefaultValue() {
        return new Report();
    }

    /**
     * Creates a new {@link XStream2} to serialize {@link Issue} instances.
     *
     * @return the stream
     */
    @Override
    XStream2 createStream() {
        XStream2 xStream2 = new XStream2();
        xStream2.registerConverter(new LineRangeListConverter(xStream2));
        xStream2.registerConverter(new TreeStringConverter());
        xStream2.registerConverter(new SeverityConverter());
        xStream2.alias("lineRange", LineRange.class);
        xStream2.alias("treeString", TreeString.class);
        xStream2.alias("issue", Issue.class);
        xStream2.alias("analysisReport", Report.class);
        return xStream2;
    }

    /**
     * {@link Converter} implementation for XStream.
     */
    @SuppressWarnings("rawtypes")
    private static final class LineRangeListConverter extends RobustCollectionConverter {
        /**
         * Creates a nex {@link LineRangeListConverter} instance.
         *
         * @param xs
         *         the stream to read from or write to
         */
        LineRangeListConverter(final XStream xs) {
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

    /**
     * Default {@link Converter} implementation for XStream that does interning scoped to one unmarshalling.
     */
    private static final class TreeStringConverter implements Converter {
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source == null ? null : source.toString());
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            TreeStringBuilder builder = (TreeStringBuilder) context.get(TreeStringBuilder.class);
            if (builder == null) {
                builder = new TreeStringBuilder();
                context.put(TreeStringBuilder.class, builder);
                // dedup at the end
                context.addCompletionCallback(builder::dedup, 0);
            }
            return builder.intern(reader.getValue());
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == TreeString.class;
        }
    }

    /**
     * Default {@link Converter} implementation for XStream that does interning scoped to one unmarshalling.
     */
    private static final class SeverityConverter implements Converter {
        @SuppressWarnings("PMD.NullAssignment")
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source instanceof Severity ? ((Severity) source).getName() : null);
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return Severity.valueOf(reader.getValue());
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == Severity.class;
        }
    }
}
