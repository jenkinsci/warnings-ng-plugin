package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import static hudson.plugins.warnings.parser.AjcParser.States.*;

/**
 * @author Tom Diamond
 */
@Extension
public class AjcParser extends AbstractWarningsParser {
    private static final long serialVersionUID = -9123765511497052454L;

    protected static final String ADVICE = "Advice";


    /**
     * Creates a new instance of {@link AjcParser}.
     */
    public AjcParser() {
        super(Messages._Warnings_AjcParser_ParserName(),
            Messages._Warnings_AjcParser_LinkName(),
            Messages._Warnings_AjcParser_TrendName());
    }

    @Override
    public Collection<FileAnnotation> parse(Reader reader) throws IOException {
        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();

        BufferedReader br = new BufferedReader(reader);
        String line;
        States state = START;
        String message = "", file = "", category = "";
        int lineNo = 0;

        while ((line = br.readLine()) != null) {
            //clean up any ESC characters (e.g. terminal colors)
            line = line.replaceAll((char)27 + "\\[.*" + (char)27 + "\\[0m", "");

            switch (state) {
                case START:
                    if (line.startsWith("[INFO] Showing AJC message detail for messages of types")) {
                        state = PARSING;
                    }
                    break;
                case PARSING:
                    if (line.startsWith("[WARNING] ")) {
                        state = PARSED_WARNING;
                        message = line.replaceAll("\\[WARNING\\] ", "");

                        if (message.contains("is deprecated") || message.contains("overrides a deprecated")) {
                            category = RegexpParser.DEPRECATION;
                        } else if (message.contains("adviceDidNotMatch")) {
                            category = AjcParser.ADVICE;
                        }
                    }
                    break;
                case PARSED_WARNING:
                    if (line.startsWith("\t")) {
                        state = PARSED_FILE;

                        int idx = line.lastIndexOf(":");
                        if (idx != -1) {
                            file = line.substring(0, idx);
                            try {
                                lineNo = Integer.parseInt(line.substring(idx + 1));
                            } catch (NumberFormatException ignored) {
                            } catch (IndexOutOfBoundsException ignored) {
                            }
                        }
                    }

                    if ("".equals(line)) {
                        if (!"".equals(message.trim())) {
                            warnings.add(new Warning(file.trim(), lineNo, getGroup(), category, message.trim()));
                        }
                        message = "";
                        file = "";
                        category = "";
                        lineNo = 0;
                        state = PARSING;
                    }

                    break;
                case PARSED_FILE:
                default:
                    if ("".equals(line)) {
                        if (!"".equals(message.trim())) {
                            warnings.add(new Warning(file.trim(), lineNo, getGroup(), category, message.trim()));
                        }
                        message = "";
                        file = "";
                        category = "";
                        lineNo = 0;
                        state = PARSING;
                    }
            }
        }

        return warnings;
    }


    enum States {
        START, PARSING, PARSED_WARNING, PARSED_FILE
    }
}
