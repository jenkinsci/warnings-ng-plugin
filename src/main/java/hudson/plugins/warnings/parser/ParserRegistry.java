package hudson.plugins.warnings.parser;

import hudson.model.Hudson;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.util.EncodingValidator;
import hudson.plugins.analysis.util.NullLogger;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.WarningsDescriptor;
import hudson.plugins.warnings.GroovyParser;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Registry of parsers that are available for the warnings plug-in.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class ParserRegistry {
    private final List<AbstractWarningsParser> parsers;
    private final Charset defaultCharset;
    private final Set<Pattern> includePatterns = Sets.newHashSet();
    private final Set<Pattern> excludePatterns = Sets.newHashSet();

    /**
     * Returns all warning parsers registered by extension points
     * {@link WarningsParser} and {@link AbstractWarningsParser}.
     *
     * @return the extension list
     */
    @SuppressWarnings("javadoc")
    private static List<AbstractWarningsParser> all() {
        Hudson instance = Hudson.getInstance();
        if (instance == null) {
            return Lists.newArrayList();
        }
        List<AbstractWarningsParser> parsers = Lists.newArrayList(instance.getExtensionList(AbstractWarningsParser.class));
        addParsersWithDeprecatedApi(instance, parsers);

        return parsers;
    }

    @SuppressWarnings("deprecation")
    private static void addParsersWithDeprecatedApi(final Hudson instance, final List<AbstractWarningsParser> parsers) {
        for (WarningsParser parser : instance.getExtensionList(WarningsParser.class)) {
            if (!(parser instanceof AbstractWarningsParser)) {
                parsers.add(new ParserAdapter(parser));
            }
        }
    }

    /**
     * Returns the available parsers as a list model.
     *
     * @return the model of the list box
     */
    public static ListBoxModel getParsersAsListModel() {
        ListBoxModel items = new ListBoxModel();
        for (ParserDescription parser : getAvailableParsers()) {
            items.add(parser.getGroup());
        }
        return items;
    }

    /**
     * Returns all available parsers groups, sorted alphabetically.
     *
     * @return all available parser names
     */
    public static List<ParserDescription> getAvailableParsers() {
        Set<String> groups = Sets.newHashSet();
        for (AbstractWarningsParser parser : getAllParsers()) {
            groups.add(parser.getGroup());
        }

        List<ParserDescription> sorted = Lists.newArrayList();
        for (String group : groups) {
            sorted.add(new ParserDescription(group, getParser(group).getParserName()));
        }
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Returns a parser for the specified group. If there is no such parser,
     * then a null object is returned.
     *
     * @param group
     *            the parser group
     * @return the parser
     */
    public static AbstractWarningsParser getParser(@CheckForNull final String group) {
        if (StringUtils.isEmpty(group)) {
            return new NullWarnigsParser("NULL");
        }

        List<AbstractWarningsParser> parsers = ParserRegistry.getParsers(group);
        if (parsers.isEmpty()) {
            return new NullWarnigsParser(group);
        }
        else {
            return parsers.get(0);
        }
    }

    /**
     * Returns a list of parsers that match the specified group name. Note that the
     * mapping of groups to parsers is one to many.
     *
     * @param group
     *            the parser group
     * @return a list of parsers, might be modified by the receiver
     */
    public static List<AbstractWarningsParser> getParsers(final String group) {
        return getParsers(Collections.singleton(group));
    }

    /**
     * Returns the parser ID which could be used as a URL.
     *
     * @param group the parser group
     * @return the ID
     */
    public static int getUrl(final String group) {
        List<AbstractWarningsParser> allParsers = getAllParsers();
        for (int number = 0; number < allParsers.size(); number++) {
            if (allParsers.get(number).isInGroup(group)) {
                return number;
            }
        }
        throw new NoSuchElementException("No parser found for group: " + group);
    }

    /**
     * Returns a list of parsers that match the specified names. Note that the
     * mapping of names to parsers is one to many.
     *
     * @param parserGroups
     *            the parser names
     * @return a list of parsers, might be modified by the receiver
     */
    public static List<AbstractWarningsParser> getParsers(final Collection<String> parserGroups) {
        List<AbstractWarningsParser> actualParsers = new ArrayList<AbstractWarningsParser>();
        for (String name : parserGroups) {
            for (AbstractWarningsParser warningsParser : getAllParsers()) {
                if (warningsParser.isInGroup(name)) {
                    actualParsers.add(warningsParser);
                }
            }
        }
        return actualParsers;
    }

    /**
     * Returns whether the specified parser exists.
     *
     * @param parserName
     *            the names to check for
     * @return true if the parser exist, <code>false</code> otherwise
     */
    public static boolean exists(final String parserName) {
        return !getParsers(parserName).isEmpty();
    }

    /**
     * Returns all available parsers. Parsers are automatically detected using
     * the extension point mechanism.
     *
     * @return all available parsers
     */
    private static List<AbstractWarningsParser> getAllParsers() {
        List<AbstractWarningsParser> parsers = Lists.newArrayList();
        parsers.add(new MsBuildParser(Messages._Warnings_PCLint_ParserName(),
                            Messages._Warnings_PCLint_LinkName(),
                            Messages._Warnings_PCLint_TrendName()));

        if (PluginDescriptor.isPluginInstalled("violations")) {
            ViolationsRegistry.addParsers(parsers);
        }
        Iterable<GroovyParser> parserDescriptions = getDynamicParserDescriptions();
        parsers.addAll(getDynamicParsers(parserDescriptions));
        parsers.addAll(all());

        return ImmutableList.copyOf(parsers);
    }

    private static Iterable<GroovyParser> getDynamicParserDescriptions() {
        Hudson instance = Hudson.getInstance();
        if (instance != null) {
            WarningsDescriptor descriptor = instance.getDescriptorByType(WarningsDescriptor.class);
            if (descriptor != null) {
                return Lists.newArrayList(descriptor.getParsers());
            }
        }
        return Collections.emptyList();
    }

    static List<AbstractWarningsParser> getDynamicParsers(final Iterable<GroovyParser> parserDescriptions) {
        List<AbstractWarningsParser> parsers = Lists.newArrayList();
        for (GroovyParser description : parserDescriptions) {
            if (description.isValid()) {
                AbstractWarningsParser parser;
                if (description.hasMultiLineSupport()) {
                    parser = new DynamicDocumentParser(description.getName(), description.getRegexp(), description.getScript(),
                            description.getLinkName(), description.getTrendName());
                }
                else {
                    parser = new DynamicParser(description.getName(), description.getRegexp(), description.getScript(),
                            description.getLinkName(), description.getTrendName());
                }
                parsers.add(parser);
            }
        }
        return parsers;
    }

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param parsers
     *            the parsers to use when scanning a file
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public ParserRegistry(final List<? extends AbstractWarningsParser> parsers, final String defaultEncoding) {
        this(parsers, defaultEncoding, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of <code>ParserRegistry</code>.
     *
     * @param parsers
     *            the parsers to use when scanning a file
     * @param includePattern
     *            Ant file-set pattern of files to include in report,
     *            <code>null</code> or an empty string do not filter the output
     * @param excludePattern
     *            Ant file-set pattern of files to exclude from report,
     *            <code>null</code> or an empty string do not filter the output
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public ParserRegistry(final List<? extends AbstractWarningsParser> parsers, final String defaultEncoding,
            final String includePattern, final String excludePattern) {
        defaultCharset = EncodingValidator.defaultCharset(defaultEncoding);
        this.parsers = new ArrayList<AbstractWarningsParser>(parsers);
        if (this.parsers.isEmpty()) {
            this.parsers.addAll(getAllParsers());
        }
        addPatterns(includePatterns, includePattern);
        addPatterns(excludePatterns, excludePattern);
    }

    private void addPatterns(final Set<Pattern> patterns, final String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            String[] splitted = StringUtils.split(pattern, ',');
            for (String singlePattern : splitted) {
                String trimmed = StringUtils.trim(singlePattern);
                String directoriesReplaced = StringUtils.replace(trimmed, "**", "*"); // NOCHECKSTYLE
                patterns.add(Pattern.compile(StringUtils.replace(directoriesReplaced, "*", ".*"))); // NOCHECKSTYLE
            }
        }
    }

    /**
     * Iterates over the available parsers and parses the specified file with each parser.
     * Returns all found warnings.
     *
     * @param file the input stream
     * @return all found warnings
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<FileAnnotation> parse(final File file) throws IOException {
        return parse(file, new NullLogger());
    }

    /**
     * Iterates over the available parsers and parses the specified file with
     * each parser. Returns all found warnings.
     *
     * @param file
     *            the input stream
     * @param logger
     *            the logger to write to
     * @return all found warnings
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Collection<FileAnnotation> parse(final File file, final PluginLogger logger) throws IOException {
        Set<FileAnnotation> allAnnotations = Sets.newHashSet();
        for (AbstractWarningsParser parser : parsers) {
            Reader input = null;
            try {
                input = createReader(file);
                Collection<FileAnnotation> warnings = parser.parse(input);
                logger.log(String.format("%s : Found %d warnings.", parser.getParserName(), warnings.size()));
                allAnnotations.addAll(warnings);
            }
            finally {
                IOUtils.closeQuietly(input);
            }
        }
        return applyExcludeFilter(allAnnotations);
    }

    /**
     * Iterates over the available parsers and parses the specified file with each parser.
     * Returns all found warnings.
     *
     * @param file the input stream
     * @return all found warnings
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Set<FileAnnotation> parse(final InputStream file) throws IOException {
        try {
            Set<FileAnnotation> allAnnotations = Sets.newHashSet();
            for (AbstractWarningsParser parser : parsers) {
                allAnnotations.addAll(parser.parse(createReader(file)));
            }
            return applyExcludeFilter(allAnnotations);
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    /**
     * Applies the exclude filter to the found annotations.
     *
     * @param allAnnotations
     *            all annotations
     * @return the filtered annotations if there is a filter defined
     */
    private Set<FileAnnotation> applyExcludeFilter(final Set<FileAnnotation> allAnnotations) {
        Set<FileAnnotation> includedAnnotations;
        if (includePatterns.isEmpty()) {
            includedAnnotations = allAnnotations;
        }
        else {
            includedAnnotations = Sets.newHashSet();
            for (FileAnnotation annotation : allAnnotations) {
                for (Pattern include : includePatterns) {
                    if (include.matcher(annotation.getFileName()).matches()) {
                        includedAnnotations.add(annotation);
                    }
                }
            }
        }
        if (excludePatterns.isEmpty()) {
            return includedAnnotations;
        }
        else {
            Set<FileAnnotation> excludedAnnotations = Sets.newHashSet(includedAnnotations);
            for (FileAnnotation annotation : includedAnnotations) {
                for (Pattern exclude : excludePatterns) {
                    if (exclude.matcher(annotation.getFileName()).matches()) {
                        excludedAnnotations.remove(annotation);
                    }
                }
            }
            return excludedAnnotations;
        }
    }

    /**
     * Creates a reader from the specified file. Uses the defined character set to
     * read the content of the input stream.
     *
     * @param file the file
     * @return the reader
     * @throws FileNotFoundException if the file does not exist
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("OBL")
    protected Reader createReader(final File file) throws FileNotFoundException {
        return createReader(new FileInputStream(file));
    }

    /**
     * Creates a reader from the specified input stream. Uses the defined character set to
     * read the content of the input stream.
     *
     * @param inputStream the input stream
     * @return the reader
     */
    protected Reader createReader(final InputStream inputStream) {
        return new InputStreamReader(inputStream, defaultCharset);
    }

    /**
     * Null object pattern.
     *
     * @author Ulli Hafner
     */
    private static final class NullWarnigsParser extends AbstractWarningsParser {
        NullWarnigsParser(final String group) {
            super(hudson.plugins.warnings.parser.Messages._Warnings_NotLocalizedName(group),
                    hudson.plugins.warnings.Messages._Warnings_ProjectAction_Name(),
                    hudson.plugins.warnings.Messages._Warnings_Trend_Name());
        }

        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public Collection<FileAnnotation> parse(final Reader reader) throws IOException,
                ParsingCanceledException {
            return Collections.emptyList();
        }
    }

}

