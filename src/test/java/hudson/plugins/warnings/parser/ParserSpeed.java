package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Speed test of the parsers in the {@link ParserRegistry}.
 */
public class ParserSpeed {
    /**
     * Runs all parsers and logs the results to the console.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testAllParsersOnOneFile() throws IOException {
        for (ParserDescription parser : ParserRegistry.getAvailableParsers()) {
            List<AbstractWarningsParser> parsers = ParserRegistry.getParsers(parser.getGroup());
            ParserRegistry parserRegistry = createRegistry(parsers);

            long start = System.currentTimeMillis();
            parserRegistry.parse(new File(""));
            long end = System.currentTimeMillis();
            System.out.println(parser.getName() + ": " + (end-start) + "ms"); // NOCHECKSTYLE NOPMD
        }
    }

    /**
     * Creates the {@link ParserRegistry}.
     *
     * @param parsers
     *            the parsers to use
     * @return the registry
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC")
    private ParserRegistry createRegistry(final List<AbstractWarningsParser> parsers) {
        ParserRegistry parserRegistry = new ParserRegistry(parsers, "", StringUtils.EMPTY, StringUtils.EMPTY) {
            /** {@inheritDoc} */
            @Override
            protected Reader createReader(final File file) throws FileNotFoundException {
                return new InputStreamReader(ParserSpeed.class.getResourceAsStream("all.txt"));
            }
        };
        return parserRegistry;
    }
}

