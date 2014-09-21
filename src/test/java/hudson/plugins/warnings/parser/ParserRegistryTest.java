package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.NullLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.warnings.GroovyParser;
import hudson.plugins.warnings.GroovyParserTest;

/**
 * Tests the class {@link ParserRegistry}.
 */
public class ParserRegistryTest {
    private static final String UNDEFINED = "undefined";
    private static final File DUMMY_FILE = new File("");
    private static final String FILE_NAME = "all.txt";
    private static final String WRONG_NUMBER_OF_ANNOTATIONS_PARSED = "Wrong number of annotations parsed";

    /**
     * Verifies that we get a null object if the parser is not found.
     */
    @Test
    public void testNullObject() {
        AbstractWarningsParser parser = ParserRegistry.getParser(UNDEFINED);

        assertEquals("Wrong name", UNDEFINED, parser.getParserName().toString());
        assertEquals("Wrong link",
                hudson.plugins.warnings.Messages._Warnings_ProjectAction_Name().toString(),
                parser.getLinkName().toString());
        assertEquals("Wrong trend",
                hudson.plugins.warnings.Messages._Warnings_Trend_Name().toString(),
                parser.getTrendName().toString());
    }

    /**
     * Checks whether we correctly find all Oracle invalids in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testOracleInvalidsParser() throws IOException {
        List<AbstractWarningsParser> parsers = new ArrayList<AbstractWarningsParser>();
        parsers.add(new InvalidsParser());
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, parsers);

        Collection<FileAnnotation> annotations = parserRegistry.parse(DUMMY_FILE);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 2, annotations.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(annotations);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 2, result.getNumberOfAnnotations());

        result.addAnnotations(annotations);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 2, result.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly find all warnings of two parsers in the log file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testTwoParsers() throws IOException {
        List<AbstractWarningsParser> parsers = new ArrayList<AbstractWarningsParser>();
        parsers.add(new InvalidsParser());
        parsers.add(new JavaDocParser());
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, parsers);

        Collection<FileAnnotation> annotations = parserRegistry.parse(DUMMY_FILE);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 10, annotations.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(annotations);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 10, result.getNumberOfAnnotations());

        result.addAnnotations(annotations);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 10, result.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly find all warnings in the log file. The file
     * contains 18 additional ANT warnings, 8 of them should be excluded by the single
     * pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-2359">Issue 2359</a>
     */
    @Test
    public void issue2359() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, createJavaParsers());

        Collection<FileAnnotation> annotations = parseAndFilter(parserRegistry, DUMMY_FILE, StringUtils.EMPTY, ".*/clover.*");
        int excludedNumberOfWarnings = 8;
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, computeTotalNumberOfWarnings(createJavaParsers()) - excludedNumberOfWarnings, annotations.size());
    }

    private Collection<FileAnnotation> parseAndFilter(final ParserRegistry parserRegistry, final File file,
                                                      final String includePattern, final String excludePattern) throws IOException {
        Collection<FileAnnotation> annotations = parserRegistry.parse(file);
        return new WarningsFilter().apply(annotations, includePattern, excludePattern, new NullLogger());
    }

    /**
     * Checks that we do not count duplicates.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-7775">Issue 7775</a>
     */
    @Test
    public void issue7775() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest("issue7775.txt", Lists.newArrayList(new MsBuildParser()));

        Collection<FileAnnotation> annotations = parserRegistry.parse(DUMMY_FILE);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED,  57, annotations.size());
    }

    private List<AbstractWarningsParser> createJavaParsers() {
        List<AbstractWarningsParser> parsers = new ArrayList<AbstractWarningsParser>();
        parsers.add(new JavacParser());
        parsers.add(new AntJavacParser());
        return parsers;
    }

    private int computeTotalNumberOfWarnings(final List<AbstractWarningsParser> parsers) throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, parsers);

        return parserRegistry.parse(DUMMY_FILE).size();
    }

    /**
     * The file contains 18 additional ANT warnings, 15 of them should be excluded by the
     * two patterns.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-2359">Issue 2359</a>
     */
    @Test
    public void multiplePatternsIssue2359() throws IOException {
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, createJavaParsers());

        Collection<FileAnnotation> annotations = parseAndFilter(parserRegistry, DUMMY_FILE,
                StringUtils.EMPTY, ".*/clover.*/.*, .*/renderers/.*");
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, computeTotalNumberOfWarnings(createJavaParsers()) - 15, annotations.size());
    }

    /**
     * Checks whether we correctly find all warnings in the log file. The file
     * contains 18 additional ANT warnings, 8 of them should be included by the single
     * pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-3866">Issue 3866</a>
     */
    @Test
    public void issue3866() throws IOException {
        List<AbstractWarningsParser> parsers = new ArrayList<AbstractWarningsParser>();
        parsers.add(new AntJavacParser());
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, parsers);

        Collection<FileAnnotation> annotations = parseAndFilter(parserRegistry, DUMMY_FILE,
                "/tmp/clover*/**", StringUtils.EMPTY);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 8, annotations.size());
    }

    /**
     * The file contains 18 additional ANT warnings, 15 of them should be included by the
     * two patterns.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-3866">Issue 3866</a>
     */
    @Test
    public void multiplePatternsIssue3866() throws IOException {
        List<AbstractWarningsParser> parsers = new ArrayList<AbstractWarningsParser>();
        parsers.add(new AntJavacParser());
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, parsers);

        Collection<FileAnnotation> annotations = parseAndFilter(parserRegistry, DUMMY_FILE,
                "/tmp/clover*/**, **/renderers/*", StringUtils.EMPTY);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 15, annotations.size());
    }

    /**
     * The file contains 18 additional ANT warnings, 8 of them should be included by
     * one pattern, 7 of them should be excluded by one pattern.
     *
     * @throws IOException
     *             if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-3866">Issue 3866</a>
     */
    @Test
    public void complexFilterIssue3866() throws IOException {
        List<AbstractWarningsParser> parsers = new ArrayList<AbstractWarningsParser>();
        parsers.add(new AntJavacParser());
        ParserRegistry parserRegistry = createRegistryUnderTest(FILE_NAME, parsers);

        Collection<FileAnnotation> annotations = parseAndFilter(parserRegistry, DUMMY_FILE,
                "/tmp/clover*/**", "**/renderers/*");
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS_PARSED, 1, annotations.size());
    }

    /**
     * Creates the registry under test.
     *
     * @param fileName
     *            file name with the warnings
     * @param parsers
     *            the parsers to use
     * @return the registry
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
    private ParserRegistry createRegistryUnderTest(final String fileName,
                                                   final List<? extends AbstractWarningsParser> parsers) {
        ParserRegistry parserRegistry = new ParserRegistry(parsers, StringUtils.EMPTY) {
                    @Override
            protected Reader createReader(final File file) throws FileNotFoundException {
                return new InputStreamReader(ParserRegistryTest.class.getResourceAsStream(fileName));
            }
        };
        return parserRegistry;
    }

    /**
     * Tests the construction of dynamic parsers.
     */
    @Test
    public void testDynamicParsers() {
        GroovyParser multi = new GroovyParser("name", GroovyParserTest.MULTI_LINE_REGEXP, "empty");
        GroovyParser single = new GroovyParser("name", GroovyParserTest.SINGLE_LINE_REGEXP, "empty");

        List<AbstractWarningsParser> allParsers = ParserRegistry.getDynamicParsers(Lists.newArrayList(single, multi));

        int multiNumber = 0;
        int singleNumber = 0;

        for (AbstractWarningsParser parser : allParsers) {
            if (parser.getClass() == DynamicParser.class) {
                singleNumber++;
            }
            else if (parser.getClass() == DynamicDocumentParser.class) {
                multiNumber++;
            }
        }

        assertEquals("Wrong number of single line parsers" , 1, singleNumber);
        assertEquals("Wrong number of multi line parsers" , 1, multiNumber);
    }
}

