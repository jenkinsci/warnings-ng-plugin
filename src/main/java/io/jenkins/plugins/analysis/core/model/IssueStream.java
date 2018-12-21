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
 * Provides an {@link XStream} with predefined converters and aliases for {@link Issue} instances.
 *
 * @author Ullrich Hafner
 */
public class IssueStream {
    /**
     * Creates a new {@link XStream2} to serialize {@link Issue} instances.
     *
     * @return the stream
     */
    XStream2 createStream() {
        XStream2 xStream2 = new XStream2();
        xStream2.registerConverter(new LineRangeListConverter(xStream2));
        xStream2.registerConverter(new TreeStringConverter(xStream2));
        xStream2.registerConverter(new SeverityConverter(xStream2));
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
    public static final class LineRangeListConverter extends RobustCollectionConverter {
        /**
         * Creates a nex {@link LineRangeListConverter} instance.
         *
         * @param xs
         *         the stream to read from or write to
         */
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

    /**
     * Default {@link Converter} implementation for XStream that does interning scoped to one unmarshalling.
     */
    @SuppressWarnings("all")
    public static final class TreeStringConverter implements Converter {
        public TreeStringConverter(final XStream xs) {
        }

        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source == null ? null : source.toString());
        }

        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            TreeStringBuilder builder = (TreeStringBuilder) context.get(TreeStringBuilder.class);
            if (builder == null) {
                context.put(TreeStringBuilder.class, builder = new TreeStringBuilder());

                // dedup at the end
                final TreeStringBuilder _builder = builder;
                context.addCompletionCallback(new Runnable() {
                    public void run() {
                        _builder.dedup();
                    }
                }, 0);
            }
            return builder.intern(reader.getValue());
        }

        public boolean canConvert(final Class type) {
            return type == TreeString.class;
        }
    }

    /**
     * Default {@link Converter} implementation for XStream that does interning scoped to one unmarshalling.
     */
    @SuppressWarnings("all")
    public static final class SeverityConverter implements Converter {
        public SeverityConverter(final XStream xs) {
        }

        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source instanceof Severity ? ((Severity) source).getName() : null);
        }

        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return Severity.valueOf(reader.getValue());
        }

        public boolean canConvert(final Class type) {
            return type == Severity.class;
        }
    }

}
