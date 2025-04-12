package io.jenkins.plugins.analysis.core.model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.hm.hafner.analysis.DuplicationGroup;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.Ensure;
import edu.hm.hafner.util.LineRange;
import edu.hm.hafner.util.LineRangeList;
import edu.hm.hafner.util.TreeString;

import java.util.Collection;

import hudson.util.RobustCollectionConverter;
import hudson.util.RobustReflectionConverter;
import hudson.util.XStream2;

import io.jenkins.plugins.util.AbstractXmlStream;

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
    public Report createDefaultValue() {
        return new Report();
    }

    @Override
    protected void configureXStream(final XStream2 xStream) {
        xStream.registerConverter(new ReportConverter(xStream.getMapper(), xStream.getReflectionProvider()));
        xStream.registerConverter(new LineRangeListConverter(xStream));
        xStream.registerConverter(new SeverityConverter());
        xStream.alias("lineRange", LineRange.class);
        xStream.alias("edu.hm.hafner.analysis.LineRangeList", LineRangeList.class);
        xStream.alias("edu.hm.hafner.analysis.LineRange", LineRange.class);
        xStream.alias("edu.hm.hafner.analysis.parser.dry.DuplicationGroup", DuplicationGroup.class);
        xStream.alias("treeString", TreeString.class);
        xStream.alias("issue", Issue.class);
        xStream.alias("analysisReport", Report.class);
    }

    /**
     * {@link Converter} implementation for XStream.
     */
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
    private static final class SeverityConverter implements Converter {
        @SuppressWarnings("PMD.NullAssignment")
        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            writer.setValue(source instanceof Severity s ? s.getName() : null);
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

    /**
     * A dummy converter for {@link Report} instances that simply delegates to a {@link RobustReflectionConverter}. This
     * workaround is required since {@link Report} instances now have custom serialization methods.
     */
    private static class ReportConverter implements Converter {
        private final RobustReflectionConverter ref;

        ReportConverter(final Mapper mapper, final ReflectionProvider reflectionProvider) {
            ref = new RobustReflectionConverter(mapper, reflectionProvider);
        }

        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            ref.marshal(source, writer, context);
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return ref.unmarshal(reader, context);
        }

        @Override
        public boolean canConvert(final Class type) {
            return type == Report.class;
        }
    }
}
