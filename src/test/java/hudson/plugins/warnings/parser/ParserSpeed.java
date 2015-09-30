package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Speed test of the parsers in the {@link ParserRegistry}.
 */
public class ParserSpeed {
    private static final String TEST = "test";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    /**
     * Runs all parsers and logs the results to the console.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testAllParsersOnOneFile() throws IOException {
        List<ParserDescription> availableParsers = ParserRegistry.getAvailableParsers();
        for (ParserDescription parser : availableParsers) {
            List<AbstractWarningsParser> parsers = ParserRegistry.getParsers(parser.getGroup());
            ParserRegistry parserRegistry = createRegistry(parsers);

            long start = System.currentTimeMillis();
            try {
                parserRegistry.parse(new File(""));
                long end = System.currentTimeMillis();
                System.out.println(parser.getName() + ": " + (end - start) + "ms"); // NOCHECKSTYLE NOPMD
            }
            catch (Exception exception) {
                System.out.println(parser.getName() + ": Exception"); // NOCHECKSTYLE NOPMD
            }
        }
    }

    /**
     * Measures the performance of the dynamic parser.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-14383">Issue 14383</a>
     */
    @Test
    public void issue14383() throws IOException {
        DynamicParser dynamicParser = new DynamicParser(TEST, "^([A-Z]+):(\\s)*\\[(.*)\\].*at:\\s(\\w+\\.java)\\((\\d+)\\)(\\s)*$",
                "import hudson.plugins.warnings.parser.Warning\n"
                        + "return new Warning(\"fileName\", 1, \"Dynamic Parser\", \"category\" , \"normal\")",
                TEST, TEST);
        long start = System.currentTimeMillis();
        dynamicParser.parse(openFile("issue14383.txt"));
        long end = System.currentTimeMillis();
        System.out.println("Dynamic parser: " + (end-start) + "ms"); // NOCHECKSTYLE NOPMD
    }

    /**
     * Returns an input stream with the warnings.
     *
     * @param fileName
     *            the file to read
     * @return an input stream
     */
    @SuppressWarnings("Dm")
    protected Reader openFile(final String fileName) {
        try {
            return new InputStreamReader(ParserTester.class.getResourceAsStream(fileName), "UTF-8");
        }
        catch (UnsupportedEncodingException exception) {
            return new InputStreamReader(ParserTester.class.getResourceAsStream(fileName));
        }
    }

    /**
     * Creates the {@link ParserRegistry}.
     *
     * @param parsers
     *            the parsers to use
     * @return the registry
     */
    @SuppressWarnings("SIC")
    private ParserRegistry createRegistry(final List<AbstractWarningsParser> parsers) {
        ParserRegistry parserRegistry = new ParserRegistry(parsers, StringUtils.EMPTY) {
            @Override
            @SuppressWarnings("Dm")
            protected Reader createReader(final File file) throws FileNotFoundException {
                return new InputStreamReader(ParserSpeed.class.getResourceAsStream("all.txt"));
            }
        };
        return parserRegistry;
    }
}

