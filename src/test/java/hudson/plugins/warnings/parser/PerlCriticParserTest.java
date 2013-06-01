package hudson.plugins.warnings.parser;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * FIXME: Document type PerlCriticParserTest.
 *
 * @author Ulli Hafner
 */
public class PerlCriticParserTest extends ParserTester {

    @Test
    public void simpleTest() throws IOException {
        Collection<FileAnnotation> warnings = parse("perlcritic.txt");
        System.out.println(warnings.size());
        for (FileAnnotation annotation : warnings) {
            System.out.println("+++++++++++++++++ANNTATION BEGIN+++++++++++++++++++");
            System.out.println(annotation.getFileName());
            System.out.println(annotation.getMessage());
            System.out.println(annotation.getPriority());
            System.out.println(annotation.getCategory());
            System.out.println("+++++++++++++++++ANNOTATION END++++++++++++++++++++");
        }
    }

    private Collection<FileAnnotation> parse(final String fileName) throws IOException {
        return new PerlCriticParser().parse(openFile(fileName));
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "perlcritic.txt";
    }

}

