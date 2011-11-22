package hudson.plugins.warnings.parser;

import hudson.console.ConsoleNote;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Creates a console annotation.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:OFF
public class CreateConsoleAnnotations {
    @SuppressWarnings("unchecked")
    public static void main(final String[] args) throws IOException {
        List<String> lines = IOUtils.readLines(EclipseParserTest.class.getResourceAsStream("eclipse.txt"));

        FileOutputStream file = new FileOutputStream("/home/hafner/eclipse-console-note.txt");

        for (String line : lines) {
            IOUtils.write(ConsoleNote.PREAMBLE_STR, file);
            IOUtils.write(String.valueOf(line.hashCode()), file);
            IOUtils.write(ConsoleNote.POSTAMBLE_STR, file);
            IOUtils.write(line, file);
            IOUtils.write("\n", file);
        }
    }
// CHECKSTYLE:ON
}

