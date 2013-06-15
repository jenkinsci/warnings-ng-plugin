package hudson.plugins.warnings.parser.jcreportparser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import hudson.Extension;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.parser.AbstractWarningsParser;
import hudson.plugins.warnings.parser.Messages;
import hudson.plugins.warnings.parser.ParsingCanceledException;
import hudson.plugins.warnings.parser.Warning;

import hudson.util.IOException2;

/**
 * JcReportParser-Class. This class parses from the jcReport.xml and creates warnings from its content.
 *
 * @author Johann Vierthaler, johann.vierthaler@web.de
 */
@Extension
public class JcReportParser extends AbstractWarningsParser {

    /**
     * Generated document field serialVersionUID.
     */
    private static final long serialVersionUID = -1302787609831475403L;

    /**
     * Creates a new instance of JcReportParser.
     *
     * @author Johann Vierthaler, johann.vierthaler@web.de
     * @NOTE: Only the Super-Constructor is called with 3 Localizables.
     */
    public JcReportParser() {
        super(Messages._Warnings_JCReport_ParserName(), Messages._Warnings_JCReport_LinkName(), Messages
                ._Warnings_JCReport_TrendName());
    }

    /**
     * Inherited from the super-class.
     *
     * @author Johann Vierthaler, johann.vierthaler@web.de This overwritten method passes the reader to createReport()
     *         and starts adding all the warnings to the Collection that will be returned at the end of the method.
     * @return warnings -> the collection of Warnings parsed from the Report.
     * @param reader
     *            -> the reader that parses from the source-file.
     * @exception IOException
     *                -> thrown by createReport()
     * @exception ParsingCanceledException
     *                -> thrown by createReport()
     */
    @Override
    public Collection<FileAnnotation> parse(final Reader reader) throws IOException, ParsingCanceledException {
        final Report report = createReport(reader);
        List<FileAnnotation> warnings = new ArrayList<FileAnnotation>();

        for (int i = 0; i < report.getFiles().size(); i++) {

            final File file = report.getFiles().get(i);

            for (int j = 0; j < file.getItems().size(); j++) {
                final Item item = file.getItems().get(j);
                final Warning warning = createWarning(file.getName(), getLineNumber(item.getLine()),
                        item.getFindingtype(), item.getMessage(), getPriority(item.getSeverity()));

                warning.setOrigin(item.getOrigin());
                warning.setPackageName(file.getPackageName());
                warning.setPathName(file.getSrcdir());
                warning.setColumnPosition(getLineNumber(item.getColumn()), getLineNumber(item.getEndcolumn()));
                warnings.add(warning);
            }
        }
        return warnings;
    }

    /**
     * The severity-level parsed from the JcReport will be matched with a priority.
     *
     * @author Johann Vierthaler, johann.vierthaler@web.de
     * @return priority -> the priority-enum matching with the issueLevel.
     * @param issueLevel
     *            -> the severity-level parsed from the JcReport.
     */
    private Priority getPriority(final String issueLevel) {

        if (issueLevel == null || issueLevel.isEmpty()) {
            return Priority.HIGH;
        }

        if (issueLevel.contains("CriticalError")) {
            return Priority.HIGH;
        }
        else if (issueLevel.contains("Error")) {
            return Priority.HIGH;
        }
        else if (issueLevel.contains("CriticalWarning")) {
            return Priority.HIGH;
        }
        else if (issueLevel.contains("Warning")) {
            return Priority.NORMAL;
        }
        else {
            return Priority.LOW;
        }

    }

    /**
     * Creates a Report-Object out of the content within the JcReport.xml.
     *
     * @param source
     *            -> the Reader-object that is the source to build the Report-Object.
     * @return report -> the finished Report-Object that creates the Warnings.
     * @throws IOException
     *             -> due to digester.parse(new InputSource(source))
     */
    public Report createReport(final Reader source) throws IOException {
        try {
            final DigesterLoader digesterLoader = DigesterLoader.newLoader(new JcReportModule());
            final Digester digester = digesterLoader.newDigester();
            return digester.parse(new InputSource(source));
        }

        catch (SAXException exception) {
            throw new IOException2(exception);
        }
    }

}
